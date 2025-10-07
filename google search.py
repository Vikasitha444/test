from duckduckgo_search import DDGS

def search_web(query):
    results = DDGS().text(query, max_results=10)
    return results


results = search_web("Galadari")

for i, result in enumerate(results, 1):
    print(f"{i}. {result['title']}")
    print(f"   {result['href']}")
    print(f"   {result['body']}\n")