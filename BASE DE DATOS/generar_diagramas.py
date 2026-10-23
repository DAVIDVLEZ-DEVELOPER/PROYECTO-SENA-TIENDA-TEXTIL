import os

def generar_svg_modelo_relacional():
    # Dimensiones y estilo base
    width = 1750
    height = 1250
    
    svg = []
    svg.append(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}" width="100%" height="100%" style="background:#0f172a; font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica Neue, Arial, sans-serif;">')
    
    # Definiciones de gradientes, filtros y marcadores de flecha
    svg.append('''
    <defs>
      <!-- Patrón de cuadrícula de fondo -->
      <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
        <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#1e293b" stroke-width="0.8"/>
      </pattern>
      
      <!-- Sombra suave para las tarjetas de tablas -->
      <filter id="card-shadow" x="-5%" y="-5%" width="115%" height="115%">
        <feDropShadow dx="0" dy="8" stdDeviation="6" flood-color="#000000" flood-opacity="0.5"/>
      </filter>

      <!-- Marcador para claves foráneas (flecha) -->
      <marker id="fk-arrow" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
        <polygon points="0 0, 10 3.5, 0 7" fill="#38bdf8" />
      </marker>
      
      <!-- Marcador de inicio (pata de gallo / circulo) -->
      <marker id="circle-marker" markerWidth="8" markerHeight="8" refX="4" refY="4">
        <circle cx="4" cy="4" r="3" fill="#38bdf8" />
      </marker>

      <!-- Gradientes para encabezados de módulos -->
      <linearGradient id="grad-catalogo" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#0284c7" />
        <stop offset="100%" stop-color="#0369a1" />
      </linearGradient>
      
      <linearGradient id="grad-ventas" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#059669" />
        <stop offset="100%" stop-color="#047857" />
      </linearGradient>
      
      <linearGradient id="grad-usuarios" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#7c3aed" />
        <stop offset="100%" stop-color="#6d28d9" />
      </linearGradient>

      <linearGradient id="grad-erp" x1="0%" y1="0%" x2="100%" y2="0%">
        <stop offset="0%" stop-color="#ea580c" />
        <stop offset="100%" stop-color="#c2410c" />
      </linearGradient>
    </defs>
    ''')
    
    # Cuadrícula de fondo
    svg.append(f'<rect width="{width}" height="{height}" fill="url(#grid)" />')
    
    # Título Principal y Subtítulo
    svg.append('''
    <g transform="translate(60, 45)">
      <rect x="0" y="0" width="1630" height="75" rx="10" fill="#1e293b" stroke="#334155" stroke-width="1.5" filter="url(#card-shadow)"/>
      <text x="30" y="34" font-size="22" font-weight="bold" fill="#f8fafc">MODELO RELACIONAL DE BASE DE DATOS (POSTGRESQL 17)</text>
      <text x="30" y="58" font-size="14" fill="#94a3b8">Plataforma D&amp;D Textil · Normalización 3FN · Integridad Referencial PK/FK · 25 Entidades Relacionales</text>
      
      <!-- Badges informativos a la derecha -->
      <g transform="translate(1100, 22)">
        <rect x="0" y="0" width="120" height="30" rx="6" fill="#0369a1"/>
        <text x="60" y="20" font-size="12" font-weight="bold" fill="#ffffff" text-anchor="middle">SENA ADSO</text>
        
        <rect x="135" y="0" width="170" height="30" rx="6" fill="#047857"/>
        <text x="220" y="20" font-size="12" font-weight="bold" fill="#ffffff" text-anchor="middle">PostgreSQL 17 (UTF-8)</text>

        <rect x="320" y="0" width="170" height="30" rx="6" fill="#7c3aed"/>
        <text x="405" y="20" font-size="12" font-weight="bold" fill="#ffffff" text-anchor="middle">Normalización 3FN</text>
      </g>
    </g>
    ''')

    # Función auxiliar para renderizar una tabla relacional
    def render_table(x, y, w, title, header_grad, columns):
        h = 38 + len(columns) * 24 + 10
        out = []
        out.append(f'<g transform="translate({x}, {y})" filter="url(#card-shadow)">')
        # Fondo de la tarjeta
        out.append(f'<rect width="{w}" height="{h}" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2"/>')
        # Encabezado con gradiente
        out.append(f'<path d="M 0 8 Q 0 0 8 0 L {w-8} 0 Q {w} 0 {w} 8 L {w} 34 L 0 34 Z" fill="url(#{header_grad})"/>')
        # Título de la tabla
        out.append(f'<text x="14" y="22" font-size="14" font-weight="bold" fill="#ffffff" letter-spacing="0.5">{title}</text>')
        # Línea separadora
        out.append(f'<line x1="0" y1="34" x2="{w}" y2="34" stroke="#475569" stroke-width="1"/>')
        
        # Filas de columnas
        cy = 52
        for col in columns:
            name, ctype, badge = col
            # Indicador de tipo de clave
            if badge == "PK":
                out.append(f'<rect x="12" y="{cy-11}" width="24" height="15" rx="3" fill="#fbbf24"/>')
                out.append(f'<text x="24" y="{cy}" font-size="9" font-weight="bold" fill="#0f172a" text-anchor="middle">PK</text>')
            elif badge == "FK":
                out.append(f'<rect x="12" y="{cy-11}" width="24" height="15" rx="3" fill="#38bdf8"/>')
                out.append(f'<text x="24" y="{cy}" font-size="9" font-weight="bold" fill="#0f172a" text-anchor="middle">FK</text>')
            elif badge == "UK":
                out.append(f'<rect x="12" y="{cy-11}" width="24" height="15" rx="3" fill="#a78bfa"/>')
                out.append(f'<text x="24" y="{cy}" font-size="9" font-weight="bold" fill="#0f172a" text-anchor="middle">UK</text>')
            elif badge == "PK/FK":
                out.append(f'<rect x="10" y="{cy-11}" width="36" height="15" rx="3" fill="#f43f5e"/>')
                out.append(f'<text x="28" y="{cy}" font-size="8" font-weight="bold" fill="#ffffff" text-anchor="middle">PK/FK</text>')
            else:
                out.append(f'<circle cx="24" cy="{cy-4}" r="2.5" fill="#64748b"/>')
            
            # Nombre de la columna
            col_x = 52 if badge == "PK/FK" else (42 if badge else 36)
            font_weight = "bold" if badge == "PK" else "normal"
            fill_color = "#f8fafc" if badge else "#cbd5e1"
            out.append(f'<text x="{col_x}" y="{cy}" font-size="12" font-weight="{font_weight}" fill="{fill_color}">{name}</text>')
            
            # Tipo de dato (alineado a la derecha)
            out.append(f'<text x="{w-12}" y="{cy}" font-size="11" fill="#94a3b8" text-anchor="end">{ctype}</text>')
            cy += 24
            
        out.append('</g>')
        return "\n".join(out)

    # -------------------------------------------------------------
    # 1. MÓDULO CATÁLOGO & TEXTIL (Azul)
    # -------------------------------------------------------------
    svg.append(render_table(60, 150, 240, "CATEGORIAS", "grad-catalogo", [
        ("id", "SERIAL", "PK"),
        ("nombre", "VARCHAR(100)", "UK"),
        ("descripcion", "TEXT", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(340, 150, 280, "PRODUCTOS", "grad-catalogo", [
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

    svg.append(render_table(60, 310, 240, "IMAGENES_PRODUCTO", "grad-catalogo", [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("url_imagen", "TEXT", ""),
        ("orden_visualizacion", "INTEGER", "")
    ]))

    svg.append(render_table(60, 470, 240, "LOTES_INVENTARIO", "grad-catalogo", [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("numero_lote", "VARCHAR(50)", ""),
        ("metros_recibidos", "NUMERIC(10,2)", ""),
        ("costo_por_metro", "NUMERIC(10,2)", ""),
        ("proveedor", "VARCHAR(100)", ""),
        ("fecha_recepcion", "DATE", "")
    ]))

    svg.append(render_table(60, 710, 240, "ROLLOS_TEXTIL", "grad-catalogo", [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("lote_id", "INTEGER", "FK"),
        ("codigo_rollo", "VARCHAR(50)", "UK"),
        ("metraje_inicial", "NUMERIC(10,2)", ""),
        ("metraje_remanente", "NUMERIC(10,2)", ""),
        ("lote_tintoreria", "VARCHAR(50)", ""),
        ("estado", "VARCHAR(30)", "")
    ]))

    svg.append(render_table(340, 560, 280, "EVENTOS_MERMA", "grad-catalogo", [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("metros_perdidos", "NUMERIC(10,2)", ""),
        ("motivo", "VARCHAR(100)", ""),
        ("impacto_costo", "NUMERIC(10,2)", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(340, 780, 280, "UMBRALES_STOCK", "grad-catalogo", [
        ("id", "SERIAL", "PK"),
        ("tipo_tela", "VARCHAR(100)", "UK"),
        ("metros_minimos", "NUMERIC(10,2)", ""),
        ("alerta_activa", "BOOLEAN", "")
    ]))

    svg.append(render_table(60, 970, 560, "INVENTARIO_TELAS_ERP", "grad-catalogo", [
        ("id", "SERIAL", "PK"),
        ("sku", "VARCHAR(50)", "UK"),
        ("nombre_tela", "VARCHAR(150)", ""),
        ("categoria", "VARCHAR(100)", ""),
        ("metros_actuales", "NUMERIC(10,2)", ""),
        ("metros_minimos", "NUMERIC(10,2)", ""),
        ("costo_por_metro", "NUMERIC(10,2)", "")
    ]))

    # -------------------------------------------------------------
    # 2. MÓDULO USUARIOS & SEGURIDAD (Púrpura)
    # -------------------------------------------------------------
    svg.append(render_table(670, 150, 280, "USUARIOS", "grad-usuarios", [
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

    svg.append(render_table(670, 460, 280, "TICKETS_SOPORTE", "grad-usuarios", [
        ("id", "SERIAL", "PK"),
        ("usuario_id", "INTEGER", "FK"),
        ("correo_usuario", "VARCHAR(150)", ""),
        ("asunto", "VARCHAR(150)", ""),
        ("descripcion", "TEXT", ""),
        ("estado", "VARCHAR(30)", ""),
        ("prioridad", "VARCHAR(20)", ""),
        ("asignado_a", "INTEGER", "FK")
    ]))

    svg.append(render_table(670, 730, 280, "REPORTES_ERRORES", "grad-usuarios", [
        ("id", "SERIAL", "PK"),
        ("vendedor_id", "INTEGER", "FK"),
        ("nombre_vendedor", "VARCHAR(100)", ""),
        ("area", "VARCHAR(100)", ""),
        ("descripcion", "TEXT", ""),
        ("estado", "VARCHAR(30)", ""),
        ("prioridad", "VARCHAR(20)", "")
    ]))

    svg.append(render_table(670, 970, 280, "ACTIVIDAD_RECIENTE", "grad-usuarios", [
        ("id", "SERIAL", "PK"),
        ("tipo", "VARCHAR(50)", ""),
        ("usuario_id", "INTEGER", "FK"),
        ("nombre_usuario", "VARCHAR(100)", ""),
        ("accion", "TEXT", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    # -------------------------------------------------------------
    # 3. MÓDULO VENTAS & TRANSACCIONAL (Verde)
    # -------------------------------------------------------------
    svg.append(render_table(1000, 150, 310, "PEDIDOS", "grad-ventas", [
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

    svg.append(render_table(1000, 560, 310, "ITEMS_PEDIDO", "grad-ventas", [
        ("id", "SERIAL", "PK"),
        ("pedido_id", "INTEGER", "FK"),
        ("producto_id", "INTEGER", "FK"),
        ("cantidad", "NUMERIC(10,2)", ""),
        ("precio_por_metro", "NUMERIC(12,2)", ""),
        ("subtotal", "NUMERIC(12,2)", "")
    ]))

    svg.append(render_table(1000, 770, 310, "ITEMS_CARRITO", "grad-ventas", [
        ("id", "SERIAL", "PK"),
        ("usuario_id", "INTEGER", "FK"),
        ("producto_id", "INTEGER", "FK"),
        ("cantidad", "INTEGER", ""),
        ("agregado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(1000, 950, 310, "RESENAS", "grad-ventas", [
        ("id", "SERIAL", "PK"),
        ("producto_id", "INTEGER", "FK"),
        ("usuario_id", "INTEGER", "FK"),
        ("calificacion", "INTEGER", ""),
        ("comentario", "TEXT", ""),
        ("creado_en", "TIMESTAMP", "")
    ]))

    # -------------------------------------------------------------
    # 4. MÓDULO CUPONES, CMS & ERP (Naranja)
    # -------------------------------------------------------------
    svg.append(render_table(1360, 150, 310, "CUPONES", "grad-erp", [
        ("id", "SERIAL", "PK"),
        ("codigo", "VARCHAR(50)", "UK"),
        ("tipo_descuento", "VARCHAR(20)", ""),
        ("valor_descuento", "NUMERIC(10,2)", ""),
        ("expira_en", "TIMESTAMP", ""),
        ("compra_minima", "NUMERIC(12,2)", ""),
        ("activo", "BOOLEAN", "")
    ]))

    svg.append(render_table(1360, 380, 310, "CATEGORIAS_CUPON", "grad-erp", [
        ("id", "SERIAL", "PK"),
        ("cupon_id", "INTEGER", "FK"),
        ("categoria_id", "INTEGER", "FK")
    ]))

    svg.append(render_table(1360, 510, 310, "CONFIGURACION_SISTEMA", "grad-erp", [
        ("id", "SERIAL", "PK"),
        ("clave", "VARCHAR(100)", "UK"),
        ("valor", "TEXT", ""),
        ("actualizado_en", "TIMESTAMP", "")
    ]))

    svg.append(render_table(1360, 680, 310, "METRICAS_VENTAS_ERP", "grad-erp", [
        ("id", "SERIAL", "PK"),
        ("fecha_registro", "DATE", "UK"),
        ("ventas_reales", "NUMERIC(14,2)", ""),
        ("ventas_objetivo", "NUMERIC(14,2)", ""),
        ("margen_ganancia", "NUMERIC(5,2)", "")
    ]))

    svg.append(render_table(1360, 870, 310, "VENTAS_REGION", "grad-erp", [
        ("id", "SERIAL", "PK"),
        ("region", "VARCHAR(100)", ""),
        ("ventas_totales", "NUMERIC(14,2)", ""),
        ("porcentaje", "NUMERIC(5,2)", "")
    ]))

    svg.append(render_table(1360, 1030, 310, "VENTAS_DIARIAS", "grad-erp", [
        ("id", "SERIAL", "PK"),
        ("fecha_venta", "DATE", "UK"),
        ("ventas_totales", "NUMERIC(14,2)", ""),
        ("pedidos_totales", "INTEGER", "")
    ]))

    # -------------------------------------------------------------
    # LÍNEAS CONECTORAS DE RELACIONES (Claves Foráneas)
    # Trazado ortogonal por canales despejados (sin cruce sobre tablas)
    # -------------------------------------------------------------
    svg.append('''
    <!-- Relaciones (Lineas de conexion ortogonales con marcadores) -->
    <g stroke="#38bdf8" stroke-width="1.8" fill="none" opacity="0.85">
      <!-- CATEGORIAS -> PRODUCTOS -->
      <path d="M 300 200 L 340 200" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />
      
      <!-- USUARIOS -> PRODUCTOS (Vendedor) -->
      <path d="M 670 250 L 620 250" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- PRODUCTOS -> IMAGENES_PRODUCTO -->
      <path d="M 340 370 L 300 370" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- PRODUCTOS -> LOTES_INVENTARIO -->
      <path d="M 340 500 L 300 500" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- LOTES_INVENTARIO -> ROLLOS_TEXTIL -->
      <path d="M 180 686 L 180 710" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- PRODUCTOS -> EVENTOS_MERMA -->
      <path d="M 480 510 L 480 560" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- USUARIOS -> PEDIDOS -->
      <path d="M 950 200 L 1000 200" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- PEDIDOS -> ITEMS_PEDIDO -->
      <path d="M 1150 534 L 1150 560" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- PRODUCTOS -> ITEMS_PEDIDO (Canal despejado entre USUARIOS y TICKETS_SOPORTE) -->
      <path d="M 620 420 L 645 420 L 645 448 L 975 448 L 975 620 L 1000 620" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- USUARIOS -> TICKETS_SOPORTE -->
      <path d="M 780 438 L 780 460" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- USUARIOS -> REPORTES_ERRORES (Canal lateral izquierdo x=655) -->
      <path d="M 670 320 L 655 320 L 655 760 L 670 760" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- USUARIOS -> ACTIVIDAD_RECIENTE (Canal lateral izquierdo x=640) -->
      <path d="M 670 360 L 640 360 L 640 1000 L 670 1000" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- USUARIOS -> ITEMS_CARRITO (Canal lateral derecho x=985) -->
      <path d="M 950 260 L 985 260 L 985 810 L 1000 810" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- PRODUCTOS -> ITEMS_CARRITO (Canal despejado y1=454, x=965) -->
      <path d="M 620 454 L 965 454 L 965 835 L 1000 835" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- CUPONES -> CATEGORIAS_CUPON -->
      <path d="M 1510 366 L 1510 380" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />

      <!-- CATEGORIAS -> CATEGORIAS_CUPON (Canal perimetral superior y lateral derecho) -->
      <path d="M 200 150 L 200 130 L 1690 130 L 1690 420 L 1670 420" stroke-dasharray="4,4" marker-start="url(#circle-marker)" marker-end="url(#fk-arrow)" />
    </g>
    ''')

    # Leyenda en la esquina inferior izquierda
    svg.append('''
    <g transform="translate(60, 1150)">
      <rect width="680" height="70" rx="8" fill="#1e293b" stroke="#334155" stroke-width="1.2" filter="url(#card-shadow)"/>
      <text x="20" y="25" font-size="12" font-weight="bold" fill="#f8fafc">CONVENCIONES DEL MODELO RELACIONAL:</text>
      
      <!-- PK -->
      <rect x="20" y="38" width="26" height="18" rx="3" fill="#fbbf24"/>
      <text x="33" y="51" font-size="10" font-weight="bold" fill="#0f172a" text-anchor="middle">PK</text>
      <text x="54" y="51" font-size="12" fill="#cbd5e1">Clave Primaria</text>

      <!-- FK -->
      <rect x="160" y="38" width="26" height="18" rx="3" fill="#38bdf8"/>
      <text x="173" y="51" font-size="10" font-weight="bold" fill="#0f172a" text-anchor="middle">FK</text>
      <text x="194" y="51" font-size="12" fill="#cbd5e1">Clave Foránea</text>

      <!-- UK -->
      <rect x="300" y="38" width="26" height="18" rx="3" fill="#a78bfa"/>
      <text x="313" y="51" font-size="10" font-weight="bold" fill="#0f172a" text-anchor="middle">UK</text>
      <text x="334" y="51" font-size="12" fill="#cbd5e1">Restricción Unique</text>

      <!-- Línea FK -->
      <line x1="470" y1="47" x2="510" y2="47" stroke="#38bdf8" stroke-width="2" marker-end="url(#fk-arrow)"/>
      <circle cx="470" cy="47" r="3" fill="#38bdf8"/>
      <text x="525" y="51" font-size="12" fill="#cbd5e1">Relación (1 a Muchos)</text>
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
