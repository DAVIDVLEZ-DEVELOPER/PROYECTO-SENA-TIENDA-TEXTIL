/**
 * servicioOAuth.js — Capa de autenticación SSO para Google y Microsoft Entra ID
 * Redirige directamente al usuario a accounts.google.com o login.microsoftonline.com
 */

/**
 * Verifica que el Client ID esté configurado (no vacío ni con el valor de ejemplo "xxxx")
 */
const clientIdValido = (clientId) => Boolean(clientId) && !/x{4,}/i.test(clientId);

/**
 * Redirige directamente al usuario al portal oficial de Google Accounts para iniciar sesión
 */
export const redirigirAGoogleAccounts = () => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || '';
    if (!clientIdValido(clientId)) {
        alert('El inicio de sesión con Google no está configurado.\nDefine VITE_GOOGLE_CLIENT_ID en el archivo .env con un Client ID real de Google Cloud Console y reinicia el servidor (npm run dev).');
        return;
    }
    const redirectUri = window.location.origin + '/login';

    const params = new URLSearchParams({
        client_id: clientId,
        redirect_uri: redirectUri,
        response_type: 'token',
        scope: 'openid email profile',
        include_granted_scopes: 'true',
        state: 'google_sso',
        prompt: 'select_account'
    });

    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`;
    window.location.href = googleAuthUrl;
};

/**
 * Redirige directamente al usuario al portal oficial de Microsoft para iniciar sesión
 */
export const redirigirAMicrosoft = () => {
    const clientId = import.meta.env.VITE_AZURE_CLIENT_ID || '';
    if (!clientIdValido(clientId)) {
        alert('El inicio de sesión con Microsoft no está configurado.\nDefine VITE_AZURE_CLIENT_ID en el archivo .env con un Application (client) ID real de Azure Portal y reinicia el servidor (npm run dev).');
        return;
    }
    const tenantId = import.meta.env.VITE_AZURE_TENANT_ID || 'common';
    const redirectUri = window.location.origin + '/login';

    const params = new URLSearchParams({
        client_id: clientId,
        response_type: 'token',
        redirect_uri: redirectUri,
        scope: 'openid profile email User.Read',
        response_mode: 'fragment',
        state: 'microsoft_sso',
        prompt: 'select_account'
    });

    const microsoftAuthUrl = `https://login.microsoftonline.com/${tenantId}/oauth2/v2.0/authorize?${params.toString()}`;
    window.location.href = microsoftAuthUrl;
};

/**
 * Extrae y procesa los datos del perfil si la URL contiene un token de retorno de Google o Microsoft
 */
export const procesarRetornoSSO = async () => {
    const hash = window.location.hash;
    if (!hash || !hash.includes('access_token')) {
        return null;
    }

    const params = new URLSearchParams(hash.substring(1));
    const accessToken = params.get('access_token');
    const state = params.get('state');

    // Limpiar el token de la barra de direcciones por seguridad
    window.history.replaceState(null, '', window.location.pathname);

    if (!accessToken) return null;

    if (state === 'microsoft_sso') {
        const meRes = await fetch('https://graph.microsoft.com/v1.0/me', {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        if (!meRes.ok) throw new Error('Error al consultar perfil en Microsoft Graph');
        const me = await meRes.json();
        return {
            proveedor: 'microsoft',
            token: accessToken,
            email: me.mail || me.userPrincipalName,
            nombre: me.displayName || (me.mail || '').split('@')[0]
        };
    } else {
        const res = await fetch('https://www.googleapis.com/oauth2/v3/userinfo', {
            headers: { Authorization: `Bearer ${accessToken}` }
        });
        if (!res.ok) throw new Error('Error al consultar perfil en Google');
        const profile = await res.json();
        return {
            proveedor: 'google',
            token: accessToken,
            email: profile.email,
            nombre: profile.name || (profile.email || '').split('@')[0]
        };
    }
};

export const iniciarSesionGoogle = redirigirAGoogleAccounts;
export const iniciarSesionMicrosoft = redirigirAMicrosoft;
