
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 1,
    duration: '30s',

    thresholds: {
        http_req_failed: ['rate<0.01'],
        checks: ['rate==1'],
    },
};

export default function () {
    const baseUrl = __ENV.BASE_URL;
    const response = http.get(`${baseUrl}/api/ping`);

    check(response, {
        'status is 200': (r) => {
            return r.status === 200;
        },
    });

    sleep(1);
}
