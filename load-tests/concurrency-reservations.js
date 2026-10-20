import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

/**
 * FlowCart AI — concurrency reservation burst via demo endpoint.
 *
 * Prerequisites:
 *   - backend running on BASE_URL
 *   - warehouseId / productId seeded with stock
 *
 * Example:
 *   k6 run -e BASE_URL=http://localhost:8080 \
 *     -e WAREHOUSE_ID=... -e PRODUCT_ID=... \
 *     load-tests/concurrency-reservations.js
 */

const successful = new Counter('flowcart_successful_reservations');
const failed = new Counter('flowcart_failed_reservations');

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    http_req_failed: ['rate<0.01'],
  },
};

export default function () {
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
  const payload = JSON.stringify({
    warehouseId: __ENV.WAREHOUSE_ID,
    productId: __ENV.PRODUCT_ID,
    stockQty: Number(__ENV.STOCK_QTY || 10),
    concurrentRequests: Number(__ENV.REQUESTS || 100),
  });

  const res = http.post(`${baseUrl}/api/v1/demo/concurrency-burst`, payload, {
    headers: { 'Content-Type': 'application/json' },
    timeout: '120s',
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'never oversells': (r) => {
      const body = r.json();
      return body.successfulReservations <= body.initialStock && body.consistent === true;
    },
  });

  if (res.status === 200) {
    const body = res.json();
    successful.add(body.successfulReservations);
    failed.add(body.failedReservations);
  }

  sleep(1);
}
