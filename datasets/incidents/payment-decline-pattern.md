# Payment Decline Incident Pattern

## Symptoms
- Spike in `CANCELLED` / `PAYMENT_FAILED` orders
- Payment authorize spans end with DECLINED
- Inventory reservations are released shortly after payment failure

## Likely causes
1. Chaos Lab `PAYMENT_FAILURE` scenario left enabled
2. Simulated payment provider configured to DECLINED
3. Downstream PSP outage (not simulated in Interview Core)

## Evidence checklist
- Chaos status: `paymentForceDecline=true`
- Grafana: rising `flowcart.payment.failures`
- Tempo: payment spans correlated by order id / trace id
- Knowledge RAG: this runbook citation

## Remediation
1. `POST /api/v1/chaos/reset`
2. Place a new order and pay with SUCCESS
3. Confirm order reaches `CONFIRMED` and stock is consumed
