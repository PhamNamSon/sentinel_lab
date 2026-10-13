
import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const productIds = new SharedArray('product IDs', function () {
    return JSON.parse(open('./product_ids.json'));
});

export const options = {
    stages: [
        { duration: '30s', target: 5 },
        { duration: '1m', target: 10 },
        { duration: '1m', target: 20 },
        { duration: '30s', target: 0 },
    ],

    thresholds: {
        checks: ['rate==1'],
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<500'],
    },
};

export default function () {
    const baseUrl = __ENV.BASE_URL;

    const randomIndex = Math.floor(
        Math.random() * productIds.length
    );

    const productId = productIds[randomIndex];

    const response = http.get(
        `${baseUrl}/api/product/${productId}`
    );

    check(response, {
        'status is 200': (r) => {
            return r.status === 200;
        },
    });

    sleep(1);
}
