from flask import Flask, request, render_template, jsonify
import os
import re
import requests
import time
import gc
from werkzeug.utils import secure_filename

# Try to import pymupdf (PyMuPDF)
try:
    import pymupdf
    import pymupdf4llm
    PYMUPDF_AVAILABLE = True
except ImportError:
    try:
        import fitz as pymupdf
        PYMUPDF_AVAILABLE = True
        # pymupdf4llm might not be available
        try:
            import pymupdf4llm
        except ImportError:
            pymupdf4llm = None
    except ImportError:
        PYMUPDF_AVAILABLE = False
        pymupdf = None
        pymupdf4llm = None

# Try to import OpenAI client for Groq
try:
    from openai import OpenAI
    OPENAI_AVAILABLE = True
except ImportError:
    OPENAI_AVAILABLE = False
    OpenAI = None

# Get API key from config file
try:
    from config import GROQ_API_KEY
except ImportError:
    GROQ_API_KEY = None

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'uploads'
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max

os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

# ============= Configuration from HR Survey & Job Listings =============

SRI_LANKAN_TECH_KEYWORDS = [
    "Java", "Python", "JavaScript", "C#", "C++",
    "MySQL", "PostgreSQL", "MongoDB", "Oracle",
    "Git", "GitHub", "GitLab",
    "HTML5", "CSS3", "React", "Angular", "Vue.js",
    "Node.js", "Express", "Django", "Flask", "Spring Boot",
    "Android", "iOS", "Flutter", "React Native",
    "AWS", "Azure", "Docker", "Kubernetes",
    "REST API", "GraphQL", "Microservices",
    "Agile", "Scrum", "JIRA"
]

VALUED_SOFT_SKILLS = [
    "communication", "teamwork", "leadership", "problem solving",
    "critical thinking", "time management", "adaptability", "collaboration",
    "creativity", "attention to detail", "work ethic", "interpersonal"
]

# ============= Helper function to check if PDF operations are available =============

def check_dependencies():
    """Check if required dependencies are available"""
    issues = []
    if not PYMUPDF_AVAILABLE:
        issues.append("PyMuPDF (pymupdf) is not installed. Install with: pip install pymupdf")
    if pymupdf4llm is None:
        issues.append("pymupdf4llm is not installed. Install with: pip install pymupdf4llm")
    if not OPENAI_AVAILABLE:
        issues.append("OpenAI client is not installed. Install with: pip install openai")
    return issues

# ============= Validation Functions =============

def check_cv_page_count(pdf_path):
    """Check the number of pages in the CV"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        page_count = len(doc)
        if page_count == 1:
            return {
                "status": "success",
                "message": "Perfect! Single page CV.",
                "value": "1 page ✓",
                "score": 10
            }
        elif page_count == 2:
            return {
                "status": "success",
                "message": "Good! Two pages is acceptable.",
                "value": "2 pages ✓",
                "score": 8
            }
        else:
            return {
                "status": "warning",
                "message": f"CV has {page_count} pages. Keep it to 1-2 pages.",
                "value": f"{page_count} pages",
                "score": 4
            }
    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_gpa_in_cv(pdf_path):
    """Check if GPA/CGPA is mentioned in the CV"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        full_text = ""
        for page in doc:
            full_text += page.get_text()
        text_lower = full_text.lower()

        # Look for GPA patterns
        gpa_patterns = [
            r'gpa[:\s]+([0-9.]+)',
            r'cgpa[:\s]+([0-9.]+)',
            r'grade point[:\s]+([0-9.]+)'
        ]

        gpa_found = False
        for pattern in gpa_patterns:
            if re.search(pattern, text_lower):
                gpa_found = True
                break

        if gpa_found or 'gpa' in text_lower or 'cgpa' in text_lower:
            return {
                "status": "success",
                "message": "GPA is mentioned in the CV.",
                "value": "Found ✓",
                "score": 10
            }
        else:
            return {
                "status": "warning",
                "message": "GPA not mentioned. If you have a good GPA (>3.0), add it.",
                "value": "Not found",
                "score": 5
            }
    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_professional_email(pdf_path):
    """Check if email addresses in CV are professional"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        full_text = ""
        for page in doc:
            full_text += page.get_text()

        # Extract email addresses
        email_pattern = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
        emails = re.findall(email_pattern, full_text)

        if not emails:
            return {
                "status": "error",
                "message": "No email address found in CV.",
                "value": "Missing",
                "score": 0
            }

        # Check for unprofessional patterns
        unprofessional_keywords = [
            'cool', 'hot', 'sexy', 'prince', 'king', 'boss', 'gangsta',
            'swag', 'legend', 'hero', 'cutie', 'angel', 'devil', 'ninja'
        ]

        issues = []
        for email in emails:
            email_lower = email.lower()
            local_part = email_lower.split('@')[0]

            for keyword in unprofessional_keywords:
                if keyword in local_part:
                    issues.append(f"{email} contains unprofessional word '{keyword}'")

            if re.search(r'\d{4,}', local_part):
                issues.append(f"{email} has too many consecutive numbers")

            special_chars = len(re.findall(r'[._-]', local_part))
            if special_chars > 2:
                issues.append(f"{email} has too many special characters")

        if issues:
            return {
                "status": "warning",
                "message": f"Email professionalism concerns: {'; '.join(issues[:2])}",
                "value": "Needs improvement",
                "emails": emails,
                "score": 4
            }
        else:
            return {
                "status": "success",
                "message": "Email address looks professional.",
                "value": "Professional ✓",
                "emails": emails,
                "score": 10
            }
    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_photo_presence(pdf_path):
    """Check if CV contains a photo"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        has_image = False

        for page_num in range(len(doc)):
            page = doc[page_num]
            image_list = page.get_images(full=True)
            if image_list:
                has_image = True
                break

        if has_image:
            return {
                "status": "success",
                "message": "Professional photo found (good for Sri Lankan market).",
                "value": "Present ✓",
                "score": 10
            }
        else:
            return {
                "status": "info",
                "message": "No photo detected. Consider adding a professional photo (recommended in Sri Lanka).",
                "value": "Not found",
                "score": 7
            }
    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_ol_al_presence(pdf_path):
    """Check if O/L and A/L results are present"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        full_text = ""
        for page in doc:
            full_text += page.get_text()

        text_lower = full_text.lower()

        ol_patterns = ['o/l', 'o.l', 'ordinary level', 'g.c.e o/l', 'gce o/l']
        al_patterns = ['a/l', 'a.l', 'advanced level', 'g.c.e a/l', 'gce a/l']

        has_ol = any(pattern in text_lower for pattern in ol_patterns)
        has_al = any(pattern in text_lower for pattern in al_patterns)

        if has_ol or has_al:
            details = []
            if has_ol: details.append("O/L")
            if has_al: details.append("A/L")

            return {
                "status": "warning",
                "message": f"School exam results ({', '.join(details)}) found. Sri Lankan IT companies typically don't require these for graduate positions.",
                "value": "Present (consider removing)",
                "score": 5
            }
        else:
            return {
                "status": "success",
                "message": "No school exam results found. Good - focus on degree and technical skills.",
                "value": "Not present ✓",
                "score": 10
            }
    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_formatting_quality(pdf_path):
    """Analyze CV formatting quality"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)

        issues = []
        font_sizes = set()

        for page_num in range(len(doc)):
            page = doc[page_num]
            blocks = page.get_text("dict")["blocks"]

            for block in blocks:
                if "lines" in block:
                    for line in block["lines"]:
                        for span in line["spans"]:
                            font_sizes.add(round(span["size"], 1))

        if len(font_sizes) < 2:
            issues.append("Use different font sizes for headings and body text")
        elif len(font_sizes) > 6:
            issues.append("Too many different font sizes - keep it consistent")

        page = doc[0]
        blocks = page.get_text("blocks")
        if blocks:
            min_x = min(b[0] for b in blocks)
            if min_x < 36:
                issues.append("Margins appear too small")

        score = 10 - (len(issues) * 2)
        score = max(0, min(10, score))

        if issues:
            return {
                "status": "warning",
                "message": f"Formatting suggestions: {'; '.join(issues)}",
                "value": f"{score}/10",
                "score": score
            }
        else:
            return {
                "status": "success",
                "message": "Formatting looks good.",
                "value": "Good ✓",
                "score": 10
            }
    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 5}
    finally:
        if doc:
            doc.close()


def validate_technical_keywords(pdf_path):
    """Check for market-relevant technical keywords"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        full_text = ""
        for page in doc:
            full_text += page.get_text()

        found_keywords = []
        for keyword in SRI_LANKAN_TECH_KEYWORDS:
            pattern = r'\b' + re.escape(keyword) + r'\b'
            if re.search(pattern, full_text, re.IGNORECASE):
                found_keywords.append(keyword)

        keyword_count = len(found_keywords)

        if keyword_count >= 10:
            status = "success"
            message = f"Excellent! Found {keyword_count} relevant technical keywords."
            score = 10
        elif keyword_count >= 5:
            status = "success"
            message = f"Good! Found {keyword_count} technical keywords. Consider adding more."
            score = 7
        else:
            status = "warning"
            message = f"Only {keyword_count} technical keywords found. Add more relevant technologies."
            score = 4

        return {
            "status": status,
            "message": message,
            "value": f"{keyword_count} keywords",
            "found_keywords": found_keywords[:10],
            "score": score
        }
    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def extract_github_links(text):
    """Extract GitHub repository links from text"""
    repo_pattern = r'https?://github\.com/[\w\-]+/[\w\-]+'
    repos = re.findall(repo_pattern, text)
    return list(set(repos))


def check_repository_exists(repo_url):
    """Check if a GitHub repository exists"""
    try:
        response = requests.head(repo_url, timeout=5, allow_redirects=True)
        if response.status_code == 200:
            return True, "Working"
        elif response.status_code == 404:
            return False, "Not found"
        else:
            return False, f"Error {response.status_code}"
    except requests.exceptions.Timeout:
        return False, "Timeout"
    except requests.exceptions.ConnectionError:
        return False, "Connection error"
    except Exception as e:
        return False, str(e)


def validate_github_links(pdf_path):
    """Validate GitHub links in the CV"""
    if pymupdf4llm is None:
        # Fallback: try to extract from plain text
        if not PYMUPDF_AVAILABLE:
            return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

        doc = None
        try:
            doc = pymupdf.open(pdf_path)
            full_text = ""
            for page in doc:
                full_text += page.get_text()
            repos = extract_github_links(full_text)
        except Exception as e:
            return {"status": "error", "message": f"Error: {e}", "repos": [], "value": "Error", "score": 0}
        finally:
            if doc:
                doc.close()
    else:
        try:
            markdown = pymupdf4llm.to_markdown(pdf_path)
            repos = extract_github_links(markdown)
        except Exception as e:
            return {"status": "error", "message": f"Error: {e}", "repos": [], "value": "Error", "score": 0}

    if not repos:
        return {
            "status": "warning",
            "message": "No GitHub links found. Add GitHub projects to strengthen your CV.",
            "repos": [],
            "value": "No links",
            "score": 3
        }

    valid_count = 0
    repo_details = []
    for repo in repos[:5]:  # Limit to 5 repos to avoid too many requests
        exists, message = check_repository_exists(repo)
        repo_details.append({"url": repo, "valid": exists, "message": message})
        if exists:
            valid_count += 1
        time.sleep(0.3)  # Rate limiting

    if valid_count == len(repos[:5]):
        status = "success"
        score = 10
    elif valid_count > 0:
        status = "warning"
        score = 6
    else:
        status = "error"
        score = 2

    return {
        "status": status,
        "message": f"Found {len(repos)} links, {valid_count} are working.",
        "repos": repo_details,
        "value": f"{valid_count}/{len(repos[:5])} working",
        "score": score
    }


def find_specialization(pdf_path):
    """Find the specialization mentioned in the CV"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    specializations = {
        "Software Technology": "software technology",
        "Network Technology": "network technology",
        "Multimedia Technology": "multimedia technology",
        "Software Engineering": "software engineering",
        "Computer Science": "computer science",
        "Information Technology": "information technology",
        "Data Science": "data science",
        "Cyber Security": "cyber security",
    }

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        for page_num in range(min(3, len(doc))):
            text = doc[page_num].get_text().lower()
            for spec_name, keyword in specializations.items():
                if keyword in text:
                    return {
                        "status": "success",
                        "message": f"Specialization: {spec_name}",
                        "value": spec_name,
                        "score": 10
                    }
        return {
            "status": "warning",
            "message": "Specialization area is not clearly mentioned.",
            "value": "Not specified",
            "score": 5
        }
    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_skills_separation(pdf_path):
    """Check if Technical Skills and Soft Skills are separated"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)

        soft_skills = []
        technical_skills = []
        all_blocks = []

        for page in doc:
            blocks = page.get_text("blocks")
            for b in blocks:
                all_blocks.append(b)

        all_blocks.sort(key=lambda b: (b[1], b[0]))

        sorted_lines = []
        for b in all_blocks:
            lines = b[4].splitlines()
            for line in lines:
                sorted_lines.append(line.strip())

        current_section = None

        soft_headers = ["soft skill", "soft skills", "interpersonal", "personal skills"]
        tech_headers = ["technical skill", "technical skills", "tech stack", "technologies", "programming", "skills"]

        stop_words = [
            "project", "experience", "education", "qualification",
            "reference", "contact", "certification", "certificate",
            "achievement", "profile", "summary", "declaration",
            "language", "interest", "volunteer"
        ]

        for line in sorted_lines:
            line_clean = line.strip()
            line_lower = line_clean.lower()

            if not line_clean:
                continue

            is_soft_header = any(h in line_lower for h in soft_headers) and len(line_clean) < 30
            if is_soft_header:
                current_section = "soft"
                continue

            is_tech_header = any(h in line_lower for h in tech_headers) and len(line_clean) < 30
            if is_tech_header:
                current_section = "technical"
                continue

            is_stop_header = any(w in line_lower for w in stop_words) and len(line_clean) < 30
            if is_stop_header and "skill" not in line_lower:
                current_section = None
                continue

            if current_section == "soft":
                if len(line_clean) > 2 and "skill" not in line_lower:
                    soft_skills.append(line_clean)

            elif current_section == "technical":
                if len(line_clean) > 1 and "skill" not in line_lower:
                    technical_skills.append(line_clean)

        doc.close()
        doc = None

        soft_skills = list(dict.fromkeys(soft_skills))
        technical_skills = list(dict.fromkeys(technical_skills))

        has_soft = len(soft_skills) > 0
        has_tech = len(technical_skills) > 0

        if has_soft and has_tech:
            return {
                "status": "success",
                "message": "Both Technical and Soft Skills sections found.",
                "value": "Both present ✓",
                "score": 10,
                "soft_count": len(soft_skills),
                "tech_count": len(technical_skills)
            }
        elif has_tech:
            return {
                "status": "warning",
                "message": "Technical skills found, but Soft Skills section missing.",
                "value": "Partial",
                "score": 6,
                "tech_count": len(technical_skills),
                "soft_count": 0
            }
        elif has_soft:
            return {
                "status": "warning",
                "message": "Soft Skills found, but Technical Skills section missing.",
                "value": "Partial",
                "score": 5,
                "soft_count": len(soft_skills),
                "tech_count": 0
            }
        else:
            return {
                "status": "error",
                "message": "Could not identify clear skill sections.",
                "value": "Not found",
                "score": 2,
                "soft_count": 0,
                "tech_count": 0
            }

    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_contact_information(pdf_path):
    """Check if CV has complete professional contact information"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        full_text = ""
        for page in doc:
            full_text += page.get_text()

        text_lower = full_text.lower()

        # Check for phone number
        phone_pattern = r'(\+94|0)?[\s-]?[0-9]{9,10}'
        has_phone = re.search(phone_pattern, full_text)

        # Check for email
        email_pattern = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
        has_email = re.search(email_pattern, full_text)

        # Check for LinkedIn
        has_linkedin = 'linkedin' in text_lower

        # Check for location/address
        has_location = any(word in text_lower for word in ['colombo', 'sri lanka', 'address', 'location', 'kandy', 'galle', 'jaffna'])

        score = 0
        found = []
        missing = []

        if has_phone:
            score += 3
            found.append("Phone")
        else:
            missing.append("Phone")

        if has_email:
            score += 3
            found.append("Email")
        else:
            missing.append("Email")

        if has_linkedin:
            score += 2
            found.append("LinkedIn")
        else:
            missing.append("LinkedIn")

        if has_location:
            score += 2
            found.append("Location")
        else:
            missing.append("Location")

        if score >= 8:
            return {
                "status": "success",
                "message": f"Complete contact information found: {', '.join(found)}",
                "value": "Complete ✓",
                "score": 10,
                "details": found
            }
        elif score >= 6:
            return {
                "status": "success",
                "message": f"Good contact info. Found: {', '.join(found)}",
                "value": "Good",
                "score": 8,
                "details": found
            }
        else:
            return {
                "status": "warning",
                "message": f"Missing: {', '.join(missing)}. Add complete contact information.",
                "value": "Incomplete",
                "score": max(4, score),
                "details": found
            }

    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_action_verbs(pdf_path):
    """Check if CV uses strong action verbs"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    strong_verbs = [
        'developed', 'designed', 'implemented', 'created', 'built', 'led', 'managed',
        'optimized', 'improved', 'analyzed', 'achieved', 'delivered', 'collaborated',
        'architected', 'engineered', 'deployed', 'integrated', 'automated', 'streamlined',
        'established', 'launched', 'reduced', 'increased', 'enhanced', 'maintained',
        'coordinated', 'executed', 'facilitated', 'generated', 'initiated', 'organized'
    ]

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        full_text = ""
        for page in doc:
            full_text += page.get_text()

        text_lower = full_text.lower()

        found_verbs = []
        for verb in strong_verbs:
            if verb in text_lower:
                found_verbs.append(verb)

        verb_count = len(found_verbs)

        if verb_count >= 8:
            return {
                "status": "success",
                "message": f"Excellent! Found {verb_count} strong action verbs.",
                "value": f"{verb_count} verbs ✓",
                "score": 10,
                "verbs": found_verbs[:10]
            }
        elif verb_count >= 5:
            return {
                "status": "success",
                "message": f"Good use of action verbs ({verb_count} found).",
                "value": f"{verb_count} verbs",
                "score": 7,
                "verbs": found_verbs
            }
        elif verb_count >= 3:
            return {
                "status": "warning",
                "message": f"Only {verb_count} action verbs found. Use more impact words.",
                "value": f"{verb_count} verbs",
                "score": 5,
                "verbs": found_verbs
            }
        else:
            return {
                "status": "warning",
                "message": "Very few action verbs. Use words like 'developed', 'implemented', 'designed'.",
                "value": "Weak",
                "score": 3,
                "verbs": found_verbs
            }

    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_quantifiable_achievements(pdf_path):
    """Check if CV includes numbers/metrics"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)
        full_text = ""
        for page in doc:
            full_text += page.get_text()

        # Look for numbers with context
        number_patterns = [
            r'\d+%',  # percentages
            r'\d+\+',  # 5+ years
            r'\d+\s*(projects?|users?|customers?|members?|students?)',  # countable items
            r'(increased|decreased|improved|reduced|grew)\s+.*?\d+',  # improvement metrics
            r'\d+\s*(years?|months?)',  # time periods
        ]

        metrics_found = []
        for pattern in number_patterns:
            matches = re.findall(pattern, full_text, re.IGNORECASE)
            metrics_found.extend(matches)

        metric_count = len(metrics_found)

        if metric_count >= 5:
            return {
                "status": "success",
                "message": f"Excellent! Found {metric_count} quantifiable achievements/metrics.",
                "value": f"{metric_count} metrics ✓",
                "score": 10
            }
        elif metric_count >= 3:
            return {
                "status": "success",
                "message": f"Good quantification with {metric_count} metrics.",
                "value": f"{metric_count} metrics",
                "score": 7
            }
        elif metric_count >= 1:
            return {
                "status": "warning",
                "message": f"Only {metric_count} metrics found. Add more numbers to show impact.",
                "value": f"{metric_count} metrics",
                "score": 5
            }
        else:
            return {
                "status": "warning",
                "message": "No quantifiable achievements. Add numbers (e.g., '40% improvement', '5 projects').",
                "value": "None found",
                "score": 2
            }

    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def check_professional_summary(pdf_path):
    """Check if CV has a professional summary or career objective"""
    if not PYMUPDF_AVAILABLE:
        return {"status": "error", "message": "PyMuPDF not installed", "value": "Error", "score": 0}

    doc = None
    try:
        doc = pymupdf.open(pdf_path)

        first_page = doc[0]
        text = first_page.get_text().lower()

        summary_keywords = [
            'summary', 'profile', 'objective', 'career objective',
            'professional summary', 'about me', 'introduction', 'overview'
        ]

        has_summary = any(keyword in text for keyword in summary_keywords)

        blocks = first_page.get_text("blocks")
        if blocks:
            page_height = first_page.rect.height
            top_section = page_height * 0.3

            has_top_summary = False
            for block in blocks:
                if block[1] < top_section:
                    block_text = block[4].lower()
                    if any(keyword in block_text for keyword in summary_keywords):
                        has_top_summary = True
                        break

            if has_top_summary:
                return {
                    "status": "success",
                    "message": "Professional summary found at top of CV.",
                    "value": "Present ✓",
                    "score": 10
                }
            elif has_summary:
                return {
                    "status": "success",
                    "message": "Summary section found.",
                    "value": "Present",
                    "score": 8
                }
            else:
                return {
                    "status": "info",
                    "message": "No professional summary. Consider adding a brief career objective.",
                    "value": "Not found",
                    "score": 6
                }

        return {
            "status": "warning",
            "message": "Could not detect professional summary.",
            "value": "Not detected",
            "score": 5
        }

    except Exception as e:
        return {"status": "error", "message": f"Error: {e}", "value": "Error", "score": 0}
    finally:
        if doc:
            doc.close()


def validate_with_llm(pdf_path, validation_results):
    """Enhanced LLM validation with HR manager perspective"""
    if not OPENAI_AVAILABLE:
        return {
            "status": "error",
            "message": "OpenAI client not installed. Install with: pip install openai",
            "results": [],
            "passed": 0,
            "total": 10,
            "score": 0
        }

    if pymupdf4llm is None:
        # Fallback to plain text extraction
        if not PYMUPDF_AVAILABLE:
            return {
                "status": "error",
                "message": "PyMuPDF not installed",
                "results": [],
                "passed": 0,
                "total": 10,
                "score": 0
            }
        doc = None
        try:
            doc = pymupdf.open(pdf_path)
            markdown_text = ""
            for page in doc:
                markdown_text += page.get_text() + "\n"
        except Exception as e:
            return {
                "status": "error",
                "message": f"Error reading PDF: {e}",
                "results": [],
                "passed": 0,
                "total": 10,
                "score": 0
            }
        finally:
            if doc:
                doc.close()
    else:
        try:
            markdown_text = pymupdf4llm.to_markdown(pdf_path)
        except Exception as e:
            return {
                "status": "error",
                "message": f"Error converting PDF: {e}",
                "results": [],
                "passed": 0,
                "total": 10,
                "score": 0
            }

    # Build context from other validations
    context = f"""
VALIDATION CONTEXT (from automated checks):
- Photo detected: {'YES' if validation_results['photo']['status'] == 'success' else 'NO'}
- Email found: {'YES' if validation_results['professional_email']['status'] != 'error' else 'NO'}
- Email status: {validation_results['professional_email']['value']}
- Page count: {validation_results['page_count']['value']}
- O/L & A/L present: {'YES' if validation_results['ol_al']['status'] == 'warning' else 'NO'}
- Skills separation: {validation_results['skills']['value']}
- GitHub links: {validation_results['github']['value']}
- Technical keywords found: {validation_results['keywords']['value']}
"""

    query = f'''You are an HR manager at a leading Sri Lankan IT company (like Dialog Axiata, IFS, Virtusa, WSO2, or CodeGen International).

You are reviewing this student's CV for an internship/junior developer position. Evaluate based on Sri Lankan IT industry standards.

{context}

CV Content:
{markdown_text[:8000]}

Analyze and answer YES or NO to each criterion:

1) Does it have a professional photo? (Use the validation context - if photo detected = YES, answer YES)
2) Is the email address professional? (Not like "coolguy123@gmail.com" - check if it looks professional)
3) Are computer/technical skills clearly shown in a dedicated section?
4) Are projects described with technologies used? (e.g., "Built using React, Node.js")
5) Does it look clean, organized, and well-formatted?
6) Did they appropriately handle school exam results (O/L, A/L)? (Answer YES if O/L & A/L are NOT present, NO if they are present)
7) Is it the right length (1-2 pages for intern/junior position)?
8) Are required technical keywords present? (Java, Python, MySQL, Git, HTML5, CSS3, React, etc.)
9) Is the degree "Bachelor of Information and Communication Technology (Hons)" or similar mentioned?
10) Are references present with proper contact information?

Answer ONLY with YES or NO for each numbered item. One answer per line.'''

    groq_api_key = GROQ_API_KEY or os.environ.get("GROQ_API_KEY", "")
    if not groq_api_key:
        return {
            "status": "error",
            "message": "API key missing. Please set GROQ_API_KEY in config.py.",
            "results": [],
            "passed": 0,
            "total": 10,
            "score": 0
        }

    try:
        client = OpenAI(
            api_key=groq_api_key,
            base_url="https://api.groq.com/openai/v1",
        )

        start_time = time.time()
        response = client.chat.completions.create(
            messages=[{"role": "user", "content": query}],
            model="llama-3.3-70b-versatile",
            temperature=0.1
        )
        execution_time = time.time() - start_time
        answers = response.choices[0].message.content.strip().splitlines()

        criteria = [
            ("Professional Photo", "Does it have a professional photo?"),
            ("Professional Email", "Is the email address professional?"),
            ("Technical Skills Section", "Are technical skills clearly shown?"),
            ("Project Technologies", "Are technologies mentioned in projects?"),
            ("Clean Formatting", "Is the CV well-formatted and organized?"),
            ("School Results Handling", "Are O/L & A/L results appropriately handled?"),
            ("Appropriate Length", "Is it 1-2 pages long?"),
            ("Technical Keywords", "Are required technical keywords present?"),
            ("Degree Mentioned", "Is the ICT degree mentioned?"),
            ("References Present", "Are references included?")
        ]

        llm_results = []
        passed = 0
        for i, ans in enumerate(answers[:len(criteria)]):
            answer_clean = ans.strip().lower()
            is_yes = 'yes' in answer_clean or answer_clean.startswith('y')
            llm_results.append({
                "name": criteria[i][0],
                "description": criteria[i][1],
                "passed": is_yes
            })
            if is_yes:
                passed += 1

        score = round((passed / len(criteria)) * 10, 1)

        return {
            "status": "success",
            "message": f"AI Analysis complete ({execution_time:.1f}s) - HR Manager Perspective",
            "results": llm_results,
            "passed": passed,
            "total": len(criteria),
            "score": score
        }

    except Exception as e:
        return {
            "status": "error",
            "message": f"AI Analysis error: {str(e)}",
            "results": [],
            "passed": 0,
            "total": 10,
            "score": 0
        }


def calculate_overall_score(results):
    """Calculate weighted overall score from all validations"""
    weights = {
        'page_count': 0.05,
        'gpa': 0.05,
        'professional_email': 0.08,
        'photo': 0.04,
        'ol_al': 0.05,
        'formatting': 0.06,
        'specialization': 0.04,
        'github': 0.08,
        'skills': 0.08,
        'keywords': 0.10,
        'contact': 0.06,
        'action_verbs': 0.07,
        'achievements': 0.07,
        'summary': 0.05,
        'llm': 0.12
    }

    total_score = 0
    total_weight = 0

    for key, weight in weights.items():
        if key in results and 'score' in results[key]:
            total_score += results[key]['score'] * weight
            total_weight += weight

    final_score = (total_score / total_weight) if total_weight > 0 else 0

    if final_score >= 8.5:
        grade = "Excellent"
        status = "success"
    elif final_score >= 7.0:
        grade = "Good"
        status = "success"
    elif final_score >= 5.5:
        grade = "Fair"
        status = "warning"
    else:
        grade = "Needs Improvement"
        status = "warning"

    return {
        "status": status,
        "score": round(final_score, 1),
        "grade": grade,
        "message": f"Overall CV Score: {round(final_score, 1)}/10 - {grade}"
    }


def allowed_file(filename):
    """Check if file is a PDF"""
    return '.' in filename and filename.rsplit('.', 1)[1].lower() == 'pdf'


# ============= Routes =============

@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == 'POST':
        # Check dependencies first
        dep_issues = check_dependencies()
        if dep_issues:
            return render_template("index.html",
                                 results=None,
                                 error=f"Missing dependencies: {'; '.join(dep_issues)}")

        if 'cv_file' not in request.files:
            return render_template("index.html",
                                 results=None,
                                 error="කරුණාකර CV file එකක් select කරන්න")

        file = request.files['cv_file']
        if file.filename == '':
            return render_template("index.html",
                                 results=None,
                                 error="කරුණාකර CV file එකක් select කරන්න")

        if not allowed_file(file.filename):
            return render_template("index.html",
                                 results=None,
                                 error="PDF files පමණක් accept කරනවා")

        if file:
            filename = secure_filename(file.filename)
            filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
            file.save(filepath)

            try:
                # Run all validations except LLM first
                results = {
                    'page_count': check_cv_page_count(filepath),
                    'gpa': check_gpa_in_cv(filepath),
                    'professional_email': check_professional_email(filepath),
                    'photo': check_photo_presence(filepath),
                    'ol_al': check_ol_al_presence(filepath),
                    'formatting': check_formatting_quality(filepath),
                    'specialization': find_specialization(filepath),
                    'github': validate_github_links(filepath),
                    'skills': check_skills_separation(filepath),
                    'keywords': validate_technical_keywords(filepath),
                    'contact': check_contact_information(filepath),
                    'action_verbs': check_action_verbs(filepath),
                    'achievements': check_quantifiable_achievements(filepath),
                    'summary': check_professional_summary(filepath)
                }

                # Run LLM validation with context from other validations
                results['llm'] = validate_with_llm(filepath, results)

                # Calculate overall score
                results['overall'] = calculate_overall_score(results)

            finally:
                gc.collect()
                time.sleep(0.5)
                try:
                    if os.path.exists(filepath):
                        os.remove(filepath)
                except PermissionError:
                    print(f"Warning: Could not delete {filepath} due to file lock.")

            return render_template("index.html", results=results, filename=filename)

    return render_template("index.html", results=None)


@app.route('/api/validate', methods=['POST'])
def api_validate():
    """API endpoint for programmatic CV validation"""
    dep_issues = check_dependencies()
    if dep_issues:
        return jsonify({"error": f"Missing dependencies: {'; '.join(dep_issues)}"}), 500

    if 'cv_file' not in request.files:
        return jsonify({"error": "No file provided"}), 400

    file = request.files['cv_file']
    if not allowed_file(file.filename):
        return jsonify({"error": "Only PDF files accepted"}), 400

    filename = secure_filename(file.filename)
    filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    file.save(filepath)

    try:
        results = {
            'page_count': check_cv_page_count(filepath),
            'gpa': check_gpa_in_cv(filepath),
            'professional_email': check_professional_email(filepath),
            'photo': check_photo_presence(filepath),
            'ol_al': check_ol_al_presence(filepath),
            'formatting': check_formatting_quality(filepath),
            'specialization': find_specialization(filepath),
            'github': validate_github_links(filepath),
            'skills': check_skills_separation(filepath),
            'keywords': validate_technical_keywords(filepath),
            'contact': check_contact_information(filepath),
            'action_verbs': check_action_verbs(filepath),
            'achievements': check_quantifiable_achievements(filepath),
            'summary': check_professional_summary(filepath),
        }
        results['llm'] = validate_with_llm(filepath, results)
        results['overall'] = calculate_overall_score(results)
        return jsonify(results)
    finally:
        gc.collect()
        time.sleep(0.3)
        try:
            if os.path.exists(filepath):
                os.remove(filepath)
        except:
            pass


@app.route("/job-recommendations")
def job_recommendations():
    return render_template("job_recommendation.html")


@app.route("/jobrecommendation.html")
def job_recommendations_alt():
    return render_template("job_recommendation.html")


if __name__ == '__main__':
    print("\n" + "=" * 60)
    print("  CV Validator Pro - Sri Lankan IT Market")
    print("  Based on HR Survey & Job Listing Analysis")
    print("  http://127.0.0.1:5000")
    print("=" * 60)

    dep_issues = check_dependencies()
    if dep_issues:
        print("\n⚠️  WARNING: Missing dependencies:")
        for issue in dep_issues:
            print(f"   - {issue}")
        print("\nInstall all dependencies with:")
        print("   pip install pymupdf pymupdf4llm openai requests flask")

    print("\n")
    app.run(debug=True, host='0.0.0.0', port=5000)
