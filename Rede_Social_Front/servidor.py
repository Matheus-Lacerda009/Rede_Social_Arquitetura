"""Mini-servidor do front: entrega o site e repassa a API ao back (evita CORS, sem mexer no back).
Uso:  python servidor.py [porta]   (back-end rodando em localhost:8085)"""
import http.server, os, sys, urllib.request, urllib.error

BACK, PORTA = 'http://localhost:8085', 5500
PROXY = ('/api/', '/videos', '/comentarios')
IGNORAR = {'host', 'origin', 'referer', 'connection', 'accept-encoding'}

class H(http.server.SimpleHTTPRequestHandler):
    def repassar(self):
        n = int(self.headers.get('Content-Length', 0))
        corpo = self.rfile.read(n) if n else None
        cab = {k: v for k, v in self.headers.items() if k.lower() not in IGNORAR}
        try:
            r = urllib.request.urlopen(urllib.request.Request(BACK + self.path, corpo, cab, method=self.command))
        except urllib.error.HTTPError as e:
            r = e
        except Exception:
            return self.send_error(502, 'Back-end offline em ' + BACK)
        dados = r.read()
        self.send_response(r.getcode())
        self.send_header('Content-Type', r.headers.get('Content-Type', 'application/json'))
        self.send_header('Content-Length', str(len(dados)))
        self.end_headers()
        self.wfile.write(dados)

    do_POST = do_PUT = do_DELETE = repassar

    def do_GET(self):
        self.repassar() if self.path.startswith(PROXY) else super().do_GET()

if __name__ == '__main__':
    os.chdir(os.path.dirname(os.path.abspath(__file__)))
    portas = [int(sys.argv[1])] if len(sys.argv) > 1 else [PORTA, 3000, 8000, 8090, 5000, 9000]
    for porta in portas:
        try:
            srv = http.server.ThreadingHTTPServer(('127.0.0.1', porta), H)
            break
        except OSError:
            print(f'Porta {porta} indisponivel, tentando outra...')
    else:
        raise SystemExit('Nenhuma porta livre. Tente: python servidor.py 8123')
    print(f'Conecta no ar: http://localhost:{porta}  (back em {BACK})')
    srv.serve_forever()
