
import http from 'k6/http';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

const productIds = new SharedArray('product IDs', function () {
    return JSON.parse(open('./product_ids.json'));
});

export const options = {
    scenarios: {
        productReads: {
            executor: 'constant-arrival-rate',
            rate: Number(__ENV.RATE || 25),
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 20,
            maxVUs: 50,
        },
    },

    thresholds: {
        checks: ['rate==1'],
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
        dropped_iterations: ['count==0'],
    },
};

export default function () {
    const baseUrl = __ENV.BASE_URL;

    const randomIndex = Math.floor(
        Math.random() * productIds.length
    );

    const productId = productIds[randomIndex];

    const response = http.get(
        `${baseUrl}/api/product/${productId}`,
        {
            headers: {
                Host: 'sentinel.lab.internal',
            },
        }
    );

    check(response, {
        'status is 200': function (r) {
            return r.status === 200;
        },
    });
}
