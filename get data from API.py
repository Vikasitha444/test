from openai import OpenAI
import os

# Set the API key directly in the environment (or use one of the methods below)
os.environ["GROQ_API_KEY"] = "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"

client = OpenAI(
    api_key=os.environ.get("GROQ_API_KEY"),
    base_url="https://api.groq.com/openai/v1",
)

# Use chat.completions.create for Groq API
response = client.chat.completions.create(
    messages=[
        {
            "role": "user",
            "content": "Is today raining?"
        }
    ],
    model="openai/gpt-oss-120b",  # Use a valid Groq model
)

print(response.choices[0].message.content)




