import os

def generar_svg_modelo_relacional():
    # Dimensiones y estilo base elegante y desaturado
    width = 1780
    height = 1240
    
    svg = []
    svg.append(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}" width="100%" height="100%" style="background:#0b0f17; font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica Neue, Arial, sans-serif;">')
    
    svg.append('''
    <defs>
      <pattern id="grid-clean" width="40" height="40" patternUnits="userSpaceOnUse">
        <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#162032" stroke-width="0.6"/>
      </pattern>
      
      <filter id="card-shadow" x="-3%" y="-3%" width="106%" height="108%">
        <feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000000" flood-opacity="0.45"/>
      </filter>

      <marker id="fk-arrow" markerWidth="8" markerHeight="6" refX="7" refY="3" orient="auto">
        <polygon points="0 0, 7 3, 0 6" fill="#38bdf8" />
      </marker>
      
      <marker id="circle-marker" markerWidth="6" markerHeight="6" refX="3" refY="3">
        <circle cx="3" cy="3" r="2.5" fill="#38bdf8" />
      </marker>
    </defs>
    ''')
    
    svg.append(f'<rect width="{width}" height="{height}" fill="url(#grid-clean)" />')
    
    # Encabezado Banner
    svg.append('''
    <g transform="translate(60, 40)">
      <rect x="0" y="0" width="1660" height="76" rx="10" fill="#131b2a" stroke="#253347" stroke-width="1.2" filter="url(#card-shadow)"/>
      <text x="26" y="33" font-size="20" font-weight="700" fill="#f8fafc" letter-spacing="0.3">MODELO RELACIONAL DE BASE DE DATOS (POSTGRESQL 17)</text>
      <text x="26" y="56" font-size="13" fill="#94a3b8">Plataforma D&amp;D Textil · Normalización 3FN · Integridad Referencial PK/FK · 25 Entidades Relacionales</text>
      
      <g transform="translate(1120, 23)">
        <rect x="0" y="0" width="110" height="28" rx="5" fill="#1e293b" stroke="#334155" stroke-width="1"/>
        <text x="55" y="19" font-size="11.5" font-weight="600" fill="#94a3b8" text-anchor="middle">SENA ADSO</text>
        
        <rect x="125" y="0" width="165" height="28" rx="5" fill="#172554" stroke="#1e3a8a" stroke-width="1"/>
        <text x="207" y="19" font-size="11.5" font-weight="600" fill="#93c5fd" text-anchor="middle">PostgreSQL 17 (UTF-8)</text>

        <rect x="305" y="0" width="165" height="28" rx="5" fill="#064e3b" stroke="#065f46" stroke-width="1"/>
        <text x="387" y="19" font-size="11.5" font-weight="600" fill="#6ee7b7" text-anchor="middle">Normalización 3FN</text>
      </g>
    </g>
    ''')

    def render_table(x, y, w, title, accent, columns):
        h = 34 + len(columns) * 22 + 6
        out = []
        out.append(f'<g transform="translate({x}, {y})" filter="url(#card-shadow)">')
        out.append(f'<rect width="{w}" height="{h}" rx="8" fill="#131b2a" stroke="#253347" stroke-width="1.2"/>')
        out.append(f'<path d="M 0 8 Q 0 0 8 0 L {w-8} 0 Q {w} 0 {w} 8 L {w} 32 L 0 32 Z" fill="#182234"/>')
        out.append(f'<line x1="0" y1="1" x2="{w}" y2="1" stroke="{accent}" stroke-width="2" opacity="0.9"/>')
        out.append(f'<text x="12" y="21" font-size="12.5" font-weight="700" fill="#f8fafc" letter-spacing="0.4">{title}</text>')
        out.append(f'<line x1="0" y1="32" x2="{w}" y2="32" stroke="#253347" stroke-width="1"/>')
        
        cy = 48
        for i, col in enumerate(columns):
            name, ctype, badge = col
            if i % 2 == 1:
                out.append(f'<rect x="1" y="{cy-12}" width="{w-2}" height="22" fill="#0e1420" opacity="0.5"/>')
            
            if badge == "PK":
                out.append(f'<rect x="10" y="{cy-10}" width="22" height="13" rx="3" fill="#292524" stroke="#78350f" stroke-width="0.9"/>')
                out.append(f'<text x="21" y="{cy}" font-size="8.5" font-weight="700" fill="#fbbf24" text-anchor="middle">PK</text>')
                col_x, weight, color = 38, "700", "#ffffff"
            elif badge == "FK":
                out.append(f'<rect x="10" y="{cy-10}" width="22" height="13" rx="3" fill="#172554" stroke="#1e3a8a" stroke-width="0.9"/>')
                out.append(f'<text x="21" y="{cy}" font-size="8.5" font-weight="700" fill="#93c5fd" text-anchor="middle">FK</text>')
                col_x, weight, color = 38, "500", "#cbd5e1"
            elif badge == "UK":
                out.append(f'<rect x="10" y="{cy-10}" width="22" height="13" rx="3" fill="#2e1065" stroke="#4c1d95" stroke-width="0.9"/>')
                out.append(f'<text x="21" y="{cy}" font-size="8.5" font-weight="700" fill="#c084fc" text-anchor="middle">UK</text>')
                col_x, weight, color = 38, "500", "#cbd5e1"
            else:
                out.append(f'<circle cx="21" cy="{cy-4}" r="1.8" fill="#475569"/>')
                col_x, weight, color = 34, "400", "#94a3b8"
                
            out.append(f'<text x="{col_x}" y="{cy}" font-size="11.5" font-weight="{weight}" fill="{color}">{name}</text>')
            out.append(f'<text x="{w-10}" y="{cy}" font-size="10.5" fill="#64748b" text-anchor="end" font-family="ui-monospace, monospace">{ctype}</text>')
            cy += 22
            
        out.append('</g>')
        return "\n".join(out)

    acc_blue = "#38bdf8"
    acc_indigo = "#818cf8"
    acc_green = "#34d399"
    acc_amber = "#fbbf24"

    # Columna 1A: Entidades Detalle de Catalogo e Inventario
    svg.append(render_table(60, 150, 250, "CATEGORIAS", acc_blue, [
        ("id", "SERIAL", "PK"),
        ("nombre", "VARCHAR(100)", "UK"),
        ("descripcion", "TEXT", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(60, 300, 250, "IMAGENES_PRODUCTO", acc_blue, [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("url_imagen", "TEXT", ""),
        ("orden", "INTEGER", "")
    ]))

    svg.append(render_table(60, 450, 250, "LOTES_INVENTARIO", acc_blue, [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("numero_lote", "VARCHAR(50)", ""),
        ("metros_recibidos", "NUMERIC(10,2)", ""),
        ("costo_por_metro", "NUMERIC(10,2)", ""),
        ("proveedor", "VARCHAR(100)", ""),
        ("fecha_recepcion", "DATE", "")
    ]))

    svg.append(render_table(60, 670, 250, "ROLLOS_TEXTIL", acc_blue, [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("lote_id", "INTEGER", "FK"),
        ("codigo_rollo", "VARCHAR(50)", "UK"),
        ("metraje_inicial", "NUMERIC(10,2)", ""),
        ("metraje_remanente", "NUMERIC(10,2)", ""),
        ("lote_tintoreria", "VARCHAR(50)", ""),
        ("estado", "VARCHAR(30)", "")
    ]))

    # Columna 1B: Nucleo Catalogo e Inventario
    svg.append(render_table(350, 150, 270, "PRODUCTOS", acc_blue, [
        ("id", "SERIAL", "PK"),
        ("nombre", "VARCHAR(150)", ""),
        ("categoria_id", "INTEGER", "FK"),
        ("precio", "NUMERIC(12,2)", ""),
        ("vendedor_id", "INTEGER", "FK"),
        ("descripcion", "TEXT", ""),
        ("material", "VARCHAR(100)", ""),
        ("ancho", "VARCHAR(50)", ""),
        ("peso", "VARCHAR(50)", ""),
        ("stock", "INTEGER", ""),
        ("activo", "BOOLEAN", ""),
        ("estado_moderacion", "VARCHAR(20)", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(350, 510, 270, "EVENTOS_MERMA", acc_blue, [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("metros_perdidos", "NUMERIC(10,2)", ""),
        ("motivo", "VARCHAR(100)", ""),
        ("impacto_costo", "NUMERIC(10,2)", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(350, 720, 270, "UMBRALES_STOCK", acc_blue, [
        ("id", "SERIAL", "PK"),
        ("tipo_tela", "VARCHAR(100)", "UK"),
        ("metros_minimos", "NUMERIC(10,2)", ""),
        ("alerta_activa", "BOOLEAN", "")
    ]))

    svg.append(render_table(60, 910, 560, "INVENTARIO_TELAS_ERP", acc_blue, [
        ("id", "SERIAL", "PK"),
        ("sku", "VARCHAR(50)", "UK"),
        ("nombre_tela", "VARCHAR(150)", ""),
        ("categoria", "VARCHAR(100)", ""),
        ("metros_actuales", "NUMERIC(10,2)", ""),
        ("metros_minimos", "NUMERIC(10,2)", ""),
        ("costo_por_metro", "NUMERIC(10,2)", "")
    ]))

    # Columna 2: Usuarios, Seguridad y Soporte (Indigo suave)
    svg.append(render_table(670, 150, 280, "USUARIOS", acc_indigo, [
        ("id", "SERIAL", "PK"),
        ("nombre", "VARCHAR(100)", ""),
        ("correo", "VARCHAR(150)", "UK"),
        ("contrasena_hash", "VARCHAR(255)", ""),
        ("rol", "VARCHAR(30)", ""),
        ("activo", "BOOLEAN", ""),
        ("suspendido", "BOOLEAN", ""),
        ("tasa_comision", "NUMERIC(5,2)", ""),
        ("registrado_en", "TIMESTAMP", ""),
        ("ultimo_acceso", "TIMESTAMP", "")
    ]))

    svg.append(render_table(670, 445, 280, "TICKETS_SOPORTE", acc_indigo, [
        ("id", "SERIAL", "PK"),
        ("usuario_id", "INTEGER", "FK"),
        ("correo_usuario", "VARCHAR(150)", ""),
        ("asunto", "VARCHAR(150)", ""),
        ("descripcion", "TEXT", ""),
        ("estado", "VARCHAR(30)", ""),
        ("prioridad", "VARCHAR(20)", ""),
        ("asignado_a", "INTEGER", "FK")
    ]))

    svg.append(render_table(670, 700, 280, "REPORTES_ERRORES", acc_indigo, [
        ("id", "SERIAL", "PK"),
        ("vendedor_id", "INTEGER", "FK"),
        ("nombre_vendedor", "VARCHAR(100)", ""),
        ("area", "VARCHAR(100)", ""),
        ("descripcion", "TEXT", ""),
        ("estado", "VARCHAR(30)", ""),
        ("prioridad", "VARCHAR(20)", "")
    ]))

    svg.append(render_table(670, 930, 280, "ACTIVIDAD_RECIENTE", acc_indigo, [
        ("id", "SERIAL", "PK"),
        ("tipo", "VARCHAR(50)", ""),
        ("usuario_id", "INTEGER", "FK"),
        ("nombre_usuario", "VARCHAR(100)", ""),
        ("accion", "TEXT", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    # Columna 3: Ventas & Transaccional (Esmeralda suave)
    svg.append(render_table(1000, 150, 310, "PEDIDOS", acc_green, [
        ("id", "SERIAL", "PK"),
        ("usuario_id", "INTEGER", "FK"),
        ("vendedor_id", "INTEGER", "FK"),
        ("nombre_cliente", "VARCHAR(150)", ""),
        ("correo_cliente", "VARCHAR(150)", ""),
        ("direccion_envio", "TEXT", ""),
        ("ciudad_envio", "VARCHAR(100)", ""),
        ("monto_total", "NUMERIC(12,2)", ""),
        ("costo_envio", "NUMERIC(12,2)", ""),
        ("monto_descuento", "NUMERIC(12,2)", ""),
        ("estado", "VARCHAR(50)", ""),
        ("estado_pago", "VARCHAR(50)", ""),
        ("numero_guia", "VARCHAR(100)", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(1000, 535, 310, "ITEMS_PEDIDO", acc_green, [
        ("id", "SERIAL", "PK"),
        ("pedido_id", "INTEGER", "FK"),
        ("producto_id", "INTEGER", "FK"),
        ("cantidad", "NUMERIC(10,2)", ""),
        ("precio_por_metro", "NUMERIC(12,2)", ""),
        ("subtotal", "NUMERIC(12,2)", "")
    ]))

    svg.append(render_table(1000, 745, 310, "ITEMS_CARRITO", acc_green, [
        ("id", "SERIAL", "PK"),
        ("usuario_id", "INTEGER", "FK"),
        ("producto_id", "INTEGER", "FK"),
        ("cantidad", "INTEGER", ""),
        ("agregado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(1000, 930, 310, "RESENAS", acc_green, [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("usuario_id", "INTEGER", "FK"),
        ("calificacion", "INTEGER", ""),
        ("comentario", "TEXT", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    # Columna 4: Cupones, ERP & Configuracion (Ambar suave)
    svg.append(render_table(1360, 150, 310, "CUPONES", acc_amber, [
        ("id", "SERIAL", "PK"),
        ("codigo", "VARCHAR(50)", "UK"),
        ("tipo_descuento", "VARCHAR(20)", ""),
        ("valor_descuento", "NUMERIC(10,2)", ""),
        ("expira_en", "TIMESTAMP", ""),
        ("compra_minima", "NUMERIC(12,2)", ""),
        ("activo", "BOOLEAN", "")
    ]))

    svg.append(render_table(1360, 375, 310, "CATEGORIAS_CUPON", acc_amber, [
        ("id", "SERIAL", "PK"),
        ("cupon_id", "INTEGER", "FK"),
        ("categoria_id", "INTEGER", "FK")
    ]))

    svg.append(render_table(1360, 510, 310, "CONFIGURACION_SISTEMA", acc_amber, [
        ("id", "SERIAL", "PK"),
        ("clave", "VARCHAR(100)", "UK"),
        ("valor", "TEXT", ""),
        ("actualizado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(1360, 665, 310, "METRICAS_VENTAS_ERP", acc_amber, [
        ("id", "SERIAL", "PK"),
        ("fecha_registro", "DATE", "UK"),
        ("ventas_reales", "NUMERIC(14,2)", ""),
        ("ventas_objetivo", "NUMERIC(14,2)", ""),
        ("margen_ganancia", "NUMERIC(5,2)", "")
    ]))

    svg.append(render_table(1360, 845, 310, "VENTAS_REGION", acc_amber, [
        ("id", "SERIAL", "PK"),
        ("region", "VARCHAR(100)", ""),
        ("ventas_totales", "NUMERIC(14,2)", ""),
        ("porcentaje", "NUMERIC(5,2)", "")
    ]))

    svg.append(render_table(1360, 1000, 310, "VENTAS_DIARIAS", acc_amber, [
        ("id", "SERIAL", "PK"),
        ("fecha_venta", "DATE", "UK"),
        ("ventas_totales", "NUMERIC(14,2)", ""),
        ("pedidos_totales", "INTEGER", "")
    ]))

    # Lineas conectoras ortogonales limpias (sin cruce de tablas)
    svg.append('''
    <!-- Relaciones (Lineas de conexion ortogonales despejadas) -->
    <g stroke="#38bdf8" stroke-width="1.6" fill="none" opacity="0.8">
      <!-- 1. CATEGORIAS -> PRODUCTOS -->
      <path d="M 310 195 L 350 195" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />
      
      <!-- 2. USUARIOS -> PRODUCTOS (Vendedor) -->
      <path d="M 670 240 L 620 240" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 3. PRODUCTOS -> IMAGENES_PRODUCTO -->
      <path d="M 350 350 L 310 350" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 4. PRODUCTOS -> LOTES_INVENTARIO -->
      <path d="M 350 470 L 310 470" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 5. LOTES_INVENTARIO -> ROLLOS_TEXTIL -->
      <path d="M 185 643 L 185 670" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 6. PRODUCTOS -> EVENTOS_MERMA -->
      <path d="M 485 475 L 485 510" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 7. USUARIOS -> PEDIDOS (Cliente) -->
      <path d="M 950 195 L 1000 195" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 8. PEDIDOS -> ITEMS_PEDIDO -->
      <path d="M 1155 497 L 1155 535" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 9. PRODUCTOS -> ITEMS_PEDIDO (Canal despejado x=645, y=425, x=975) -->
      <path d="M 620 420 L 645 420 L 645 425 L 975 425 L 975 590 L 1000 590" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 10. USUARIOS -> TICKETS_SOPORTE -->
      <path d="M 810 409 L 810 445" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 11. USUARIOS -> REPORTES_ERRORES (Canal x=655) -->
      <path d="M 670 310 L 655 310 L 655 730 L 670 730" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 12. USUARIOS -> ACTIVIDAD_RECIENTE (Canal x=640) -->
      <path d="M 670 345 L 640 345 L 640 960 L 670 960" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 13. USUARIOS -> ITEMS_CARRITO (Canal x=985) -->
      <path d="M 950 260 L 985 260 L 985 785 L 1000 785" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 14. PRODUCTOS -> ITEMS_CARRITO (Canal y=435, x=965) -->
      <path d="M 620 435 L 965 435 L 965 810 L 1000 810" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 15. CUPONES -> CATEGORIAS_CUPON -->
      <path d="M 1515 343 L 1515 375" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- 16. CATEGORIAS -> CATEGORIAS_CUPON (Canal superior y=130 y lateral x=1695) -->
      <path d="M 185 150 L 185 130 L 1695 130 L 1695 415 L 1670 415" stroke-dasharray="4,4" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />
    </g>
    ''')

    # Leyenda en la esquina inferior izquierda
    svg.append('''
    <g transform="translate(60, 1135)">
      <rect width="660" height="60" rx="8" fill="#131b2a" stroke="#253347" stroke-width="1.2" filter="url(#card-shadow)"/>
      <text x="18" y="22" font-size="11" font-weight="700" fill="#94a3b8" letter-spacing="0.5">CONVENCIONES DEL MODELO RELACIONAL:</text>
      
      <rect x="18" y="32" width="22" height="15" rx="3" fill="#292524" stroke="#78350f" stroke-width="0.9"/>
      <text x="29" y="43" font-size="8.5" font-weight="700" fill="#fbbf24" text-anchor="middle">PK</text>
      <text x="46" y="44" font-size="11.5" fill="#cbd5e1">Clave Primaria</text>

      <rect x="155" y="32" width="22" height="15" rx="3" fill="#172554" stroke="#1e3a8a" stroke-width="0.9"/>
      <text x="166" y="43" font-size="8.5" font-weight="700" fill="#93c5fd" text-anchor="middle">FK</text>
      <text x="183" y="44" font-size="11.5" fill="#cbd5e1">Clave Foránea</text>

      <rect x="290" y="32" width="22" height="15" rx="3" fill="#2e1065" stroke="#4c1d95" stroke-width="0.9"/>
      <text x="301" y="43" font-size="8.5" font-weight="700" fill="#c084fc" text-anchor="middle">UK</text>
      <text x="318" y="44" font-size="11.5" fill="#cbd5e1">Restricción Unique</text>

      <line x1="450" y1="40" x2="485" y2="40" stroke="#38bdf8" stroke-width="1.8" marker-end="url(#fk-arrow)"/>
      <circle cx="450" cy="40" r="2.5" fill="#38bdf8"/>
      <text x="498" y="44" font-size="11.5" fill="#cbd5e1">Relación (1 a Muchos)</text>
    </g>
    ''')

    svg.append('</svg>')
    return "\n".join(svg)


def generar_svg_arquitectura_mvc():
    width = 1500
    height = 980
    
    svg = []
    svg.append(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}" width="100%" height="100%" style="background:#0b1120; font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica Neue, Arial, sans-serif;">')
    
    svg.append('''
    <defs>
      <pattern id="grid-mvc" width="30" height="30" patternUnits="userSpaceOnUse">
        <path d="M 30 0 L 0 0 0 30" fill="none" stroke="#172554" stroke-width="0.7"/>
      </pattern>

      <filter id="shadow-mvc" x="-4%" y="-4%" width="108%" height="108%">
        <feDropShadow dx="0" dy="6" stdDeviation="5" flood-color="#000000" flood-opacity="0.6"/>
      </filter>

      <marker id="arrow-down" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">
        <polygon points="0 0, 10 4, 0 8" fill="#38bdf8" />
      </marker>
      
      <marker id="arrow-up" markerWidth="10" markerHeight="8" refX="9" refY="4" orient="auto">
        <polygon points="0 0, 10 4, 0 8" fill="#34d399" />
      </marker>

      <linearGradient id="mvc-header" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#1e1b4b" />
        <stop offset="50%" stop-color="#1e293b" />
        <stop offset="100%" stop-color="#0f172a" />
      </linearGradient>

      <!-- Gradientes por Capa -->
      <linearGradient id="grad-layer-vista" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#065f46" />
        <stop offset="100%" stop-color="#047857" />
      </linearGradient>

      <linearGradient id="grad-layer-controlador" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#1e40af" />
        <stop offset="100%" stop-color="#2563eb" />
      </linearGradient>

      <linearGradient id="grad-layer-modelo" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#581c87" />
        <stop offset="100%" stop-color="#7c3aed" />
      </linearGradient>
    </defs>
    ''')

    svg.append(f'<rect width="{width}" height="{height}" fill="url(#grid-mvc)" />')

    # Header
    svg.append('''
    <g transform="translate(60, 40)">
      <rect x="0" y="0" width="1380" height="70" rx="10" fill="url(#mvc-header)" stroke="#334155" stroke-width="1.5" filter="url(#shadow-mvc)"/>
      <text x="30" y="32" font-size="22" font-weight="bold" fill="#f8fafc">ARQUITECTURA MODELO-VISTA-CONTROLADOR (MVC) Y FLUJO DE DATOS</text>
      <text x="30" y="55" font-size="14" fill="#94a3b8">Plataforma D&amp;D Textil · Desacoplamiento Completo: Frontend React (Vista), Backend Java (Controlador) y PostgreSQL (Modelo)</text>
      
      <g transform="translate(1080, 20)">
        <rect x="0" y="0" width="130" height="30" rx="6" fill="#047857"/>
        <text x="65" y="20" font-size="12" font-weight="bold" fill="#ffffff" text-anchor="middle">SENA ADSO 2026</text>
        <rect x="145" y="0" width="140" height="30" rx="6" fill="#2563eb"/>
        <text x="215" y="20" font-size="12" font-weight="bold" fill="#ffffff" text-anchor="middle">Vanilla Java 17 + React</text>
      </g>
    </g>
    ''')

    # CAPA 1: VISTA (View)
    svg.append('''
    <!-- ==================== CAPA 1: VISTA (VIEW) ==================== -->
    <g transform="translate(60, 140)" filter="url(#shadow-mvc)">
      <rect width="1380" height="190" rx="12" fill="#0f172a" stroke="#10b981" stroke-width="1.8"/>
      <!-- Header de la capa -->
      <path d="M 0 12 Q 0 0 12 0 L 1368 0 Q 1380 0 1380 12 L 1380 40 L 0 40 Z" fill="url(#grad-layer-vista)"/>
      <text x="24" y="26" font-size="16" font-weight="bold" fill="#ffffff">1. CAPA VISTA (VIEW) - Interfaz de Usuario React 18 + Vite + TailwindCSS</text>
      <text x="1350" y="26" font-size="13" font-weight="bold" fill="#a7f3d0" text-anchor="end">Cliente / Navegador Web / App Móvil</text>

      <!-- Tarjetas internas de Vista -->
      <!-- Componente 1: Tienda Pública -->
      <g transform="translate(30, 60)">
        <rect width="300" height="110" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#34d399">Tienda Pública (Catálogo)</text>
        <text x="16" y="48" font-size="12" fill="#cbd5e1">• Catalogo.jsx (Filtros, Telas)</text>
        <text x="16" y="68" font-size="12" fill="#cbd5e1">• CarritoDrawer.jsx (Subtotales)</text>
        <text x="16" y="88" font-size="12" fill="#cbd5e1">• Pago.jsx (Checkout Seguro)</text>
      </g>

      <!-- Componente 2: Paneles Administrativos -->
      <g transform="translate(360, 60)">
        <rect width="320" height="110" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#34d399">Paneles de Gestión (RBAC)</text>
        <text x="16" y="48" font-size="12" fill="#cbd5e1">• ResumenAdmin.jsx (Métricas Recharts)</text>
        <text x="16" y="68" font-size="12" fill="#cbd5e1">• ConfiguracionStock.jsx (Umbrales)</text>
        <text x="16" y="88" font-size="12" fill="#cbd5e1">• SoporteTickets.jsx (Atención Clientes)</text>
      </g>

      <!-- Componente 3: Capa de Contexto y Peticiones -->
      <g transform="translate(710, 60)">
        <rect width="320" height="110" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#34d399">Estado Global &amp; Peticiones</text>
        <text x="16" y="48" font-size="12" fill="#cbd5e1">• AuthContext.jsx (Tokens JWT/Roles)</text>
        <text x="16" y="68" font-size="12" fill="#cbd5e1">• CartContext.jsx (Persistencia Carrito)</text>
        <text x="16" y="88" font-size="12" fill="#cbd5e1">• fetch() Asíncrono / JSON Parser</text>
      </g>

      <!-- Componente 4: Cliente Móvil Android -->
      <g transform="translate(1060, 60)">
        <rect width="290" height="110" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#34d399">Cliente Móvil Android</text>
        <text x="16" y="48" font-size="12" fill="#cbd5e1">• WebViewAssetLoader (Kotlin)</text>
        <text x="16" y="68" font-size="12" fill="#cbd5e1">• Caché Local de Telas</text>
        <text x="16" y="88" font-size="12" fill="#cbd5e1">• Interfaz Táctil Optimizada</text>
      </g>
    </g>
    ''')

    # CONECTORES ENTRE VISTA Y CONTROLADOR
    svg.append('''
    <!-- Conectores Vista <-> Controlador -->
    <g transform="translate(0, 330)">
      <!-- Petición: Vista -> Controlador -->
      <path d="M 500 0 L 500 65" stroke="#38bdf8" stroke-width="3" fill="none" marker-end="url(#arrow-down)"/>
      <rect x="360" y="20" width="280" height="26" rx="5" fill="#0f172a" stroke="#0284c7" stroke-width="1.2"/>
      <text x="500" y="38" font-size="12" font-weight="bold" fill="#38bdf8" text-anchor="middle">HTTP REST: POST / GET / PUT (JSON)</text>

      <!-- Respuesta: Controlador -> Vista -->
      <path d="M 1000 65 L 1000 0" stroke="#34d399" stroke-width="3" fill="none" marker-end="url(#arrow-up)"/>
      <rect x="850" y="20" width="300" height="26" rx="5" fill="#0f172a" stroke="#059669" stroke-width="1.2"/>
      <text x="1000" y="38" font-size="12" font-weight="bold" fill="#34d399" text-anchor="middle">HTTP 200/201 (JSON DTOs / Entidades)</text>
    </g>
    ''')

    # CAPA 2: CONTROLADOR (Controller)
    svg.append('''
    <!-- ==================== CAPA 2: CONTROLADOR (CONTROLLER) ==================== -->
    <g transform="translate(60, 400)" filter="url(#shadow-mvc)">
      <rect width="1380" height="210" rx="12" fill="#0f172a" stroke="#3b82f6" stroke-width="1.8"/>
      <!-- Header de la capa -->
      <path d="M 0 12 Q 0 0 12 0 L 1368 0 Q 1380 0 1380 12 L 1380 40 L 0 40 Z" fill="url(#grad-layer-controlador)"/>
      <text x="24" y="26" font-size="16" font-weight="bold" fill="#ffffff">2. CAPA CONTROLADOR (CONTROLLER) - API REST Java 17 (Vanilla HttpServer)</text>
      <text x="1350" y="26" font-size="13" font-weight="bold" fill="#bfdbfe" text-anchor="end">Servidor Embebido · Pool 32 Hilos · Puerto 8081</text>

      <!-- Componente 1: ManejadorBase & DevSecOps -->
      <g transform="translate(30, 60)">
        <rect width="320" height="130" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#60a5fa">ManejadorBase.java (Filtros)</text>
        <text x="16" y="48" font-size="12" fill="#cbd5e1">• CORS Dinámico (Cross-Origin)</text>
        <text x="16" y="68" font-size="12" fill="#cbd5e1">• Rate Limiting (120 req/min por IP)</text>
        <text x="16" y="88" font-size="12" fill="#cbd5e1">• Trazabilidad: X-Correlation-ID</text>
        <text x="16" y="108" font-size="12" fill="#cbd5e1">• Manejo Centralizado de Excepciones</text>
      </g>

      <!-- Componente 2: Handlers de Negocio Textil -->
      <g transform="translate(380, 60)">
        <rect width="460" height="130" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#60a5fa">Manejadores de Recursos Textil &amp; Ventas</text>
        <text x="16" y="48" font-size="12" fill="#cbd5e1">• ManejadorProductos (/api/productos) &rarr; CRUD &amp; Filtros</text>
        <text x="16" y="68" font-size="12" fill="#cbd5e1">• ManejadorPedidos (/api/pedidos) &rarr; Checkout Transaccional</text>
        <text x="16" y="88" font-size="12" fill="#cbd5e1">• ManejadorInventario (/api/inventory/*) &rarr; Alertas &amp; Stock</text>
        <text x="16" y="108" font-size="12" fill="#cbd5e1">• ManejadorCarrito (/api/cart/*) &rarr; Ítems &amp; Cupones</text>
      </g>

      <!-- Componente 3: Autenticación & Observabilidad -->
      <g transform="translate(870, 60)">
        <rect width="480" height="130" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#60a5fa">Seguridad, Configuración &amp; Salud</text>
        <text x="16" y="48" font-size="12" fill="#cbd5e1">• ManejadorAutenticacion (/api/auth/login) &rarr; Hash SHA-256</text>
        <text x="16" y="68" font-size="12" fill="#cbd5e1">• ManejadorConfiguracion (/api/configuration) &rarr; Banner / CMS</text>
        <text x="16" y="88" font-size="12" fill="#cbd5e1">• ManejadorSalud (/health/live, /health/ready) &rarr; Docker Health</text>
        <text x="16" y="108" font-size="12" fill="#cbd5e1">• Serialización Google Gson con campos en español</text>
      </g>
    </g>
    ''')

    # CONECTORES ENTRE CONTROLADOR Y MODELO
    svg.append('''
    <!-- Conectores Controlador <-> Modelo -->
    <g transform="translate(0, 610)">
      <!-- Invocación: Controlador -> Modelo -->
      <path d="M 500 0 L 500 65" stroke="#38bdf8" stroke-width="3" fill="none" marker-end="url(#arrow-down)"/>
      <rect x="370" y="20" width="260" height="26" rx="5" fill="#0f172a" stroke="#0284c7" stroke-width="1.2"/>
      <text x="500" y="38" font-size="12" font-weight="bold" fill="#38bdf8" text-anchor="middle">Invoca Métodos DAO (Java Objects)</text>

      <!-- Retorno: Modelo -> Controlador -->
      <path d="M 1000 65 L 1000 0" stroke="#34d399" stroke-width="3" fill="none" marker-end="url(#arrow-up)"/>
      <rect x="850" y="20" width="300" height="26" rx="5" fill="#0f172a" stroke="#059669" stroke-width="1.2"/>
      <text x="1000" y="38" font-size="12" font-weight="bold" fill="#34d399" text-anchor="middle">Retorna Entidades de Dominio / Resultados</text>
    </g>
    ''')

    # CAPA 3: MODELO (Model)
    svg.append('''
    <!-- ==================== CAPA 3: MODELO (MODEL) ==================== -->
    <g transform="translate(60, 680)" filter="url(#shadow-mvc)">
      <rect width="1380" height="240" rx="12" fill="#0f172a" stroke="#8b5cf6" stroke-width="1.8"/>
      <!-- Header de la capa -->
      <path d="M 0 12 Q 0 0 12 0 L 1368 0 Q 1380 0 1380 12 L 1380 40 L 0 40 Z" fill="url(#grad-layer-modelo)"/>
      <text x="24" y="26" font-size="16" font-weight="bold" fill="#ffffff">3. CAPA MODELO (MODEL) - Lógica de Negocio, Persistencia JDBC y PostgreSQL 17</text>
      <text x="1350" y="26" font-size="13" font-weight="bold" fill="#e9d5ff" text-anchor="end">ACID · Transacciones · HikariCP · 25 Tablas Relacionales</text>

      <!-- Sub-bloque 1: Entidades de Dominio POJO -->
      <g transform="translate(30, 60)">
        <rect width="360" height="160" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#c084fc">Entidades de Dominio (dominio.modelos.*)</text>
        <text x="16" y="50" font-size="12" fill="#cbd5e1">• Producto (id, nombre, precio, stock, categoria)</text>
        <text x="16" y="70" font-size="12" fill="#cbd5e1">• Pedido (id, usuarioId, total, estado, guia)</text>
        <text x="16" y="90" font-size="12" fill="#cbd5e1">• Usuario (id, correo, hash, rol, comision)</text>
        <text x="16" y="110" font-size="12" fill="#cbd5e1">• LoteInventario &amp; RolloTextil (metrajes, merma)</text>
        <text x="16" y="130" font-size="12" fill="#cbd5e1">• UmbralStock &amp; VentaRegion (métricas ERP)</text>
      </g>

      <!-- Sub-bloque 2: DAOs con JDBC & HikariCP -->
      <g transform="translate(420, 60)">
        <rect width="450" height="160" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#c084fc">Acceso a Datos (infraestructura.persistencia.jdbc.*)</text>
        <text x="16" y="50" font-size="12" fill="#cbd5e1">• ProductoDAO &rarr; Consultas preparadas contra inyección SQL</text>
        <text x="16" y="70" font-size="12" fill="#cbd5e1">• PedidoDAO &rarr; Transacción atómica (BEGIN/COMMIT/ROLLBACK)</text>
        <text x="16" y="90" font-size="12" fill="#cbd5e1">• InventarioDAO &rarr; Bloqueo pesimista: SELECT ... FOR UPDATE</text>
        <text x="16" y="110" font-size="12" fill="#cbd5e1">• CarritoDAO, CuponDAO, ResenasDAO &amp; SoporteDAO</text>
        <text x="16" y="130" font-size="12" fill="#cbd5e1">• Pool de Conexiones HikariCP (20 conexiones concurrentes)</text>
      </g>

      <!-- Sub-bloque 3: Base de Datos Relacional PostgreSQL 17 -->
      <g transform="translate(900, 60)">
        <rect width="450" height="160" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>
        <text x="16" y="26" font-size="14" font-weight="bold" fill="#c084fc">Motor PostgreSQL 17 (Docker Volume)</text>
        <text x="16" y="50" font-size="12" fill="#cbd5e1">• 25 Tablas Relacionales normalizadas en 3FN</text>
        <text x="16" y="70" font-size="12" fill="#cbd5e1">• Índices B-Tree optimizados (p95 &lt;= 200ms)</text>
        <text x="16" y="90" font-size="12" fill="#cbd5e1">• Vistas: vista_catalogo_telas, vista_alertas_stock</text>
        <text x="16" y="110" font-size="12" fill="#cbd5e1">• Vistas: vista_ventas_vendedores</text>
        <text x="16" y="130" font-size="12" fill="#cbd5e1">• Persistencia física garantizada en volumen montado</text>
      </g>
    </g>
    ''')

    # Leyenda final de ciclo transaccional
    svg.append('''
    <g transform="translate(60, 935)">
      <text x="0" y="18" font-size="12" fill="#94a3b8">Leyenda del Flujo:</text>
      <line x1="120" y1="14" x2="160" y2="14" stroke="#38bdf8" stroke-width="2.5" marker-end="url(#arrow-down)"/>
      <text x="170" y="18" font-size="12" fill="#38bdf8">1. Petición del Cliente &rarr; 2. Enrutamiento del Controlador &rarr; 3. Consulta Transaccional al Modelo</text>
      
      <line x1="820" y1="14" x2="860" y2="14" stroke="#34d399" stroke-width="2.5" marker-end="url(#arrow-up)"/>
      <text x="870" y="18" font-size="12" fill="#34d399">4. Retorno de Datos del Modelo &rarr; 5. Serialización JSON del Controlador &rarr; 6. Renderizado en Vista</text>
    </g>
    ''')

    svg.append('</svg>')
    return "\n".join(svg)


if __name__ == "__main__":
    os.makedirs("BASE DE DATOS/diagramas", exist_ok=True)
    os.makedirs("documentacion/diagramas", exist_ok=True)
    
    # 1. Guardar diagrama del Modelo Relacional (ambos nombres y ubicaciones)
    svg_relacional = generar_svg_modelo_relacional()
    for dest in [
        "BASE DE DATOS/diagramas/modelo_relacional.svg",
        "BASE DE DATOS/diagramas/modelo_relacional_actualizado.svg",
        "documentacion/diagramas/modelo_relacional.svg",
        "documentacion/diagramas/modelo_relacional_actualizado.svg"
    ]:
        with open(dest, "w", encoding="utf-8") as f:
            f.write(svg_relacional)
        print(f"Generado: {dest}")

    # 2. Guardar diagrama de la Arquitectura MVC
    svg_mvc = generar_svg_arquitectura_mvc()
    for dest in [
        "BASE DE DATOS/diagramas/arquitectura_mvc.svg",
        "documentacion/diagramas/arquitectura_mvc.svg"
    ]:
        with open(dest, "w", encoding="utf-8") as f:
            f.write(svg_mvc)
        print(f"Generado: {dest}")
