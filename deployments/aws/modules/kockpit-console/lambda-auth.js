// lambda-edge-auth/index.js
const jose = require('jose');
const cookie = require('cookie');

// configuration
const COGNITO_USER_POOL_ID = '${cognito_user_pool_id}';
const COGNITO_REGION = '${cognito_region}';
const COGNITO_DOMAIN = `https://${cognito_domain}.auth.${COGNITO_REGION}.amazoncognito.com`;
const CLIENT_ID = '${cognito_client_id}';
const CLIENT_SECRET = '${cognito_client_secret}';
const JWKS_URI = 'https://cognito-idp.' + COGNITO_REGION + '.amazauthonaws.com/' + COGNITO_USER_POOL_ID + '/.well-known/jwks.json';

let jwks;

// Main handler
exports.handler = async (event) => {
    const request = event.Records[0].cf.request;
    const headers = request.headers;
    const parsedCookies = cookie.parse(
        (headers.cookie && headers.cookie[0]?.value) || ''
    );

    // Handle OAuth callback
    if (request.uri === '/auth/callback') {
        return await handleCallback(request);
    }
    // Allow health check paths and assets
    if (request.uri === '/health' || request.uri.startsWith('/assets/')) {
        return request;
    }

    // Check for existing session token
    const token = parsedCookies['id_token'];
    if (!token) {
        return redirectToLogin(request);
    }

    // Verify JWT
    try {
        if (!jwks) {
            jwks = jose.createRemoteJWKSet(new URL(JWKS_URI));
        }
        const { payload } = await jose.jwtVerify(token, jwks, {
            audience: CLIENT_ID,
        });

        // Token is valid — inject user info into headers for downstream use
        request.headers['x-user-email'] = [{ value: payload.email }];
        request.headers['x-user-name'] = [{ value: payload.name || payload['cognito:username'] }];
        request.headers['x-user-sub'] = [{ value: payload.sub }];

        return request; // Continue to S3 origin
    } catch (err) {
        console.log('JWT verification failed:', err.message);
        return redirectToLogin(request);
    }
};

function redirectToLogin(request) {
    const hostHeader = request.headers.host[0].value;
    const redirectUri = 'https://' + hostHeader + '/auth/callback';

    const querystring = new URLSearchParams({
        response_type: 'code',
        client_id: CLIENT_ID,
        redirect_uri: redirectUri,
        scope: 'openid email profile',
    });

    return {
        status: '302',
        statusDescription: 'Found',
        headers: {
            location: [{
                value: `${COGNITO_DOMAIN}/authorize?${querystring.toString()}`
            }],
        },
    };
}

async function handleCallback(request) {
    const hostHeader = request.headers.host[0].value;
    const redirectUri = 'https://' + hostHeader + '/auth/callback';

    const params = new URLSearchParams(request.querystring);
    const code = params.get('code');

    // Exchange code for tokens
    const tokenResponse = await fetch(`${COGNITO_DOMAIN}/oauth2/token`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': 'Basic ' + Buffer.from(`${CLIENT_ID}:${CLIENT_SECRET}`).toString('base64'),
        },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            code,
            redirect_uri: redirectUri,
        }),
    });

    const tokens = await tokenResponse.json();

    return {
        status: '302',
        statusDescription: 'Found',
        headers: {
            location: [{ value: '/' }],
            'set-cookie': [
                { value: `id_token=${tokens.id_token}; Path=/; Secure; HttpOnly; SameSite=Lax; Max-Age=3600` },
                { value: `access_token=${tokens.access_token}; Path=/; Secure; HttpOnly; SameSite=Lax; Max-Age=3600` },
            ],
        },
    };
}

