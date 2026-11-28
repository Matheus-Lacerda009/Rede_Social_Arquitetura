# Conecta - Front

1. Suba o back-end (`DemoApplication`, porta 8081, MySQL).
2. Nesta pasta rode: `python servidor.py`
3. Abra o endereço que o terminal mostrar (normalmente http://localhost:5500; se a porta estiver ocupada, ele tenta outras sozinho ou use `python servidor.py 8123`)

O `servidor.py` entrega o site e repassa `/api`, `/videos` e `/comentarios` ao back, porque o back não tem CORS.
O chat em tempo real conecta direto em `ws://localhost:8081/ws/chat`. Se o back mudar de porta, ajuste `BACK`/`PORTA` no
`servidor.py` e `WS_URL` no topo de `js/app.js`.
