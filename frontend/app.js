const contenedor = document.getElementById('libros');
const mensaje = document.getElementById('mensaje');
const total = document.getElementById('total');
const disponibles = document.getElementById('disponibles');
const estado = document.getElementById('estado');
const recargar = document.getElementById('recargar');

function crearElemento(etiqueta, texto, clase) {
    const elemento = document.createElement(etiqueta);
    elemento.textContent = texto;
    if (clase) {
        elemento.className = clase;
    }
    return elemento;
}

async function cargar() {
    mensaje.textContent = 'Consultando backend...';
    contenedor.innerHTML = '';

    try {
        const [librosResponse, resumenResponse, healthResponse] = await Promise.all([
            fetch('/api/libros'),
            fetch('/api/resumen'),
            fetch('/api/health')
        ]);

        if (!librosResponse.ok || !resumenResponse.ok || !healthResponse.ok) {
            throw new Error('El backend respondió con error.');
        }

        const libros = await librosResponse.json();
        const resumen = await resumenResponse.json();
        const health = await healthResponse.json();

        total.textContent = resumen.total;
        disponibles.textContent = resumen.disponibles;
        estado.textContent = health.status;
        mensaje.textContent = 'API REST conectada correctamente.';

        // Se usa textContent (no innerHTML) para que los datos de la API no se interpreten como HTML (XSS).
        libros.forEach(libro => {
            const card = document.createElement('article');
            card.className = 'card';
            card.append(
                crearElemento('h3', libro.titulo),
                crearElemento('p', libro.autor),
                crearElemento('p', libro.disponible ? 'Disponible' : 'Prestado', libro.disponible ? 'ok' : 'no')
            );
            contenedor.appendChild(card);
        });
    } catch (error) {
        estado.textContent = 'DOWN';
        mensaje.textContent = 'No fue posible conectar con el backend.';
        contenedor.innerHTML = '<article class="card"><h3>Error de conexión</h3><p>Revisa docker compose ps y los logs del backend.</p></article>';
    }
}

recargar.addEventListener('click', cargar);
cargar();
