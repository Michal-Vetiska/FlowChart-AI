# Commerce smoke script

Prerequisites: `docker compose up -d`, backend on `:8080`.

```bash
PRODUCT=$(curl -s -X POST localhost:8080/api/v1/products -H 'Content-Type: application/json' \
  -d '{"sku":"FC-DEMO","name":"Demo Widget","basePrice":100,"currency":"CZK"}')
echo "$PRODUCT"
PID=$(echo "$PRODUCT" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

WH=$(curl -s -X POST localhost:8080/api/v1/warehouses -H 'Content-Type: application/json' \
  -d '{"code":"WH1","name":"Main","location":"PRG"}')
WID=$(echo "$WH" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

curl -s -X PUT localhost:8080/api/v1/stock -H 'Content-Type: application/json' \
  -d "{\"warehouseId\":\"$WID\",\"productId\":\"$PID\",\"availableQty\":10,\"reason\":\"seed\"}"

ORDER=$(curl -s -X POST localhost:8080/api/v1/orders -H 'Content-Type: application/json' \
  -d "{\"customerEmail\":\"a@b.com\",\"customerFullName\":\"Ada\",\"currency\":\"CZK\",\"idempotencyKey\":\"k1\",\"lines\":[{\"productId\":\"$PID\",\"warehouseId\":\"$WID\",\"quantity\":1}]}")
OID=$(echo "$ORDER" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")

curl -s -X POST "localhost:8080/api/v1/orders/$OID/pay" -H 'Content-Type: application/json' \
  -d '{"paymentIdempotencyKey":"p1","outcome":"SUCCESS"}'
```
