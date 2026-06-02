import http from 'k6/http';
import { sleep, group, check } from 'k6';

export const options = {
    scenarios: {
        average_load_balancing: {
            executor: 'constant-arrival-rate',
            rate: 10, // 10 iterations/s on average
            timeUnit: '1s',
            duration: '30s',
            preAllocatedVUs: 50,
            maxVUs: 200,
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default () => {

    // STEP 1: user opens the home page
    group('1_Homepage', function () {
        const res = http.get(`${BASE_URL}/`);

        // Check that the page is loaded
        check(res, {
            'status est 200': (r) => r.status === 200,
        });

        // Think time: user reads the home page for 1 to 2 seconds
        sleep(1 + Math.random());
    });

    group('2_Owners', function () {
        const res = http.get(`${BASE_URL}/owners`);

        check(res, {
            'status est 200': (r) => r.status === 200,
            'données présentes': (r) => r.body.includes('Madison'),
        });

        sleep(1 + Math.random());
    });

    group('3_Owners_Page_2', function () {
        const res = http.get(`${BASE_URL}/owners?page=2`);

        check(res, {
            'status est 200': (r) => r.status === 200,
            'données présentes': (r) => r.body.includes('Jean Coleman'),
        });

        sleep(1 + Math.random());
    });

    group('4_Owners_Page_3', function () {
        const res = http.get(`${BASE_URL}/owners?page=3`);

        check(res, {
            'status est 200': (r) => r.status === 200,
            'données présentes': (r) => r.body.includes('Ava Anderson'),
        });

        sleep(1 + Math.random());
    });

    group('5_Owners_Page_4', function () {
        const res = http.get(`${BASE_URL}/owners?page=4`);

        check(res, {
            'status est 200': (r) => r.status === 200,
            'données présentes': (r) => r.body.includes('Ava Foster'),
        });

        sleep(1 + Math.random());
    });

    group('6_Owners_Page_5', function () {
        const res = http.get(`${BASE_URL}/owners?page=5`);

        check(res, {
            'status est 200': (r) => r.status === 200,
            'données présentes': (r) => r.body.includes('Ava Kennedy'),
        });

        sleep(1 + Math.random());
    });

    group('7_Owner_21_Details', function () {
        const res = http.get(`${BASE_URL}/owners/21`);

        check(res, {
            'status est 200': (r) => r.status === 200,
        });

        sleep(1 + Math.random());
    });

    group('8_Owner_21_New_Visit_For_Pet_61', function () {
        const url = `${BASE_URL}/owners/21/pets/61/visits/new`;
        const payload = 'date=2026-06-30&description=test&petId=61';
        const params = {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
        };

        const res = http.post(url, payload, params);

        check(res, {
            'status est 200 ou redirect': (r) => r.status === 200 || (r.status >= 300 && r.status < 400),
        });

        sleep(1 + Math.random());
    });
};
