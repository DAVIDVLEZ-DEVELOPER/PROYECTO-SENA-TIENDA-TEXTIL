import http from 'k6/http';
import { check, sleep } from 'k6';

// Configuracion de Escenarios de Carga y Estres (SENA ADSO - ISO/IEC 25010)
export const options = {
  stages: [
    { duration: '20s', target: 30 },  // Rampa de subida: 30 usuarios virtuales (VUs)
    { duration: '40s', target: 80 },  // Carga sostenida: 80 VUs simultaneos
    { duration: '30s', target: 120 }, // Pico de estres: 120 VUs simultaneos (limite Rate Limit)
    { duration: '20s', target: 0 },   // Rampa de bajada y enfriamiento
  ],
  thresholds: {
    // El 95% de las peticiones debe responder en menos de 250 milisegundos
    http_req_duration: ['p(95)<250'],
    // Tasa maxima de fallos admitida menor al 1%
    http_req_failed: ['rate<0.01'],
  },
};

const BASE_URL = __ENV.API_URL || 'http://localhost:8081';

const HEADERS = {
  'Content-Type': 'application/json',
  'Accept': 'application/json',
  'X-Correlation-ID': 'k6-load-test-session',
};

export default function () {
  // 1. Monitoreo: Liveness Probe
  const resLive = http.get(`${BASE_URL}/health/live`, { headers: HEADERS });
  check(resLive, {
    'health live status es 200': (r) => r.status === 200,
    'health live reporta UP': (r) => r.json('status') === 'UP',
  });

  // 2. Navegacion: Catalogo completo de productos textiles
  const resProductos = http.get(`${BASE_URL}/api/productos`, { headers: HEADERS });
  check(resProductos, {
    'catalogo status es 200': (r) => r.status === 200,
    'catalogo retorna arreglo': (r) => Array.isArray(r.json()),
  });

  // 3. Modulo Home: Diapositivas de Carrusel y Secciones
  const resCarrusel = http.get(`${BASE_URL}/api/carrusel`, { headers: HEADERS });
  check(resCarrusel, {
    'carrusel status es 200': (r) => r.status === 200,
  });

  const resSecciones = http.get(`${BASE_URL}/api/secciones-inicio`, { headers: HEADERS });
  check(resSecciones, {
    'secciones inicio status es 200': (r) => r.status === 200,
  });

  // 4. Modulo Pagos: Configuracion de Pasarela Wompi
  const resWompiConfig = http.get(`${BASE_URL}/api/pagos/config`, { headers: HEADERS });
  check(resWompiConfig, {
    'config wompi status es 200': (r) => r.status === 200,
    'moneda es COP': (r) => r.json('currency') === 'COP',
  });

  // 5. Inventario ERP: Consulta de Umbrales de Stock
  const resUmbrales = http.get(`${BASE_URL}/api/inventario/thresholds`, { headers: HEADERS });
  check(resUmbrales, {
    'umbrales stock status es 200': (r) => r.status === 200,
  });

  sleep(0.5); // Pausa de medio segundo simulando tiempo de lectura del usuario
}
