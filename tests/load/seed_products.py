
import json
import os
import urllib.request
from pathlib import Path

BASE_URL = os.environ["BASE_URL"]
PRODUCT_COUNT = 10000

OUTPUT_FILE = Path(__file__).parent / "product_ids.json"


def create_product(index):
    product = {
        "name": f"Test Product {index}",
        "description": f"Test Product number {index}",
        "price": 10 + index,
        "stock": 100,
    }

    request = urllib.request.Request(
        url=f"{BASE_URL}/api/product",
        data=json.dumps(product).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    with urllib.request.urlopen(request, timeout=10) as response:
        return json.load(response)


def main():
    product_ids = []

    for index in range(1, PRODUCT_COUNT + 1):
        product = create_product(index)

        product_ids.append(product["id"])

        print(f"Created product {index}: {product['id']}")

    with OUTPUT_FILE.open("w", encoding="utf-8") as file:
        json.dump(product_ids, file, indent=2)

    print(f"Saved {len(product_ids)} IDs to {OUTPUT_FILE}")


if __name__ == "__main__":
    main()
