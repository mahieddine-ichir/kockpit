const jwt = require('jsonwebtoken');
const jwksClient = require('jwks-rsa');

// Configuration
const COGNITO_USER_POOL_ID = '${cognito_user_pool_id}';
const COGNITO_REGION = '${cognito_region}';
const COGNITO_CLIENT_ID = '${cognito_client_id}';
const COGNITO_CLIENT_SECRET = '${cognito_client_secret}';
const COGNITO_DOMAIN = '${cognito_domain}';

// JWKS client for Cognito
const client = jwksClient({
    jwksUri: 'https://cognito-idp.' + COGNITO_REGION + '.amazonaws.com/' + COGNITO_USER_POOL_ID + '/.well-known/jwks.json',
    cache: true,
    cacheMaxEntries: 5,
    cacheMaxAge: 600000 // 10 minutes
});

// Get signing key
function getKey(header, callback) {
    client.getSigningKey(header.kid, (err, key) => {
        const signingKey = key.publicKey || key.rsaPublicKey;
        callback(null, signingKey);
    });
}

// Main handler
exports.handler = async (event) => {
    const request = event.Records[0].cf.request;
    const headers = request.headers;

    console.log('request.uri == '+request.uri);
    // Allow health check paths and OAuth callback
    if (request.uri === '/health' || request.uri.startsWith('/assets/')) {
        return request;
    }
    // Handle OAuth callback
    if (request.uri === '/auth/callback') {
        return await handleCallback(request);
    }

    // Handle auth/me endpoint - return user info from JWT cookie
    if (request.uri === '/auth/me') {
        return handleAuthMe(request);
    }

    try {
        // Get JWT token from Authorization header or Cookie
        let token = null;

        if (headers.authorization && headers.authorization[0]) {
            const authHeader = headers.authorization[0].value;
            if (authHeader.startsWith('Bearer ')) {
                token = authHeader.substring(7);
            }
        }

        // Check cookies for JWT token
        if (!token && headers.cookie) {
            const cookies = headers.cookie[0].value;
            const tokenMatch = cookies.match(/id_token=([^;]+)/);
            if (tokenMatch) {
                token = tokenMatch[1];
            }
        }

        if (!token) {
            return redirectToCognito(request);
        }

        // Verify JWT token
        await new Promise((resolve, reject) => {
            jwt.verify(token, getKey, {
                audience: COGNITO_CLIENT_ID,
                issuer: 'https://cognito-idp.' + COGNITO_REGION + '.amazonaws.com/' + COGNITO_USER_POOL_ID,
                algorithms: ['RS256']
            }, (err, decoded) => {
                if (err) {
                    console.log('JWT verification failed:', err);
                    reject(err);
                } else {
                    console.log('JWT verified for user:', decoded.sub);
                    // Add user info to request headers
                    request.headers['x-cognito-user'] = [{
                        key: 'X-Cognito-User',
                        value: JSON.stringify({
                            userId: decoded.sub,
                            username: decoded['cognito:username'] || decoded.sub,
                            email: decoded.email,
                            groups: decoded['cognito:groups'] || []
                        })
                    }];
                    resolve();
                }
            });
        });

        // For SPA routes, rewrite to index.html if authenticated
        // This handles direct access to React Router routes in new tabs
        if (!request.uri.startsWith('/api/') &&
            !request.uri.startsWith('/assets/') &&
            request.uri !== '/' &&
            request.uri !== '/index.html') {
            console.log('Rewriting SPA route to index.html:', request.uri);
            request.uri = '/index.html';
        }

        return request;

    } catch (error) {
        console.log('Authentication error:', error);
        return redirectToCognito(request);
    }
};

function redirectToCognito(request) {
    const hostHeader = request.headers.host[0].value;
    const redirectUri = 'https://' + hostHeader + '/auth/callback';
    const cognitoAuthUrl = 'https://' + COGNITO_DOMAIN + '.auth.' + COGNITO_REGION + '.amazoncognito.com/login?client_id=' + COGNITO_CLIENT_ID + '&response_type=code&scope=openid+profile+email&redirect_uri=' + encodeURIComponent(redirectUri);

    return {
        status: '302',
        statusDescription: 'Found',
        headers: {
            location: [{
                key: 'Location',
                value: cognitoAuthUrl
            }],
            'cache-control': [{
                key: 'Cache-Control',
                value: 'no-cache'
            }]
        }
    };
}

async function handleCallback(request) {
    const hostHeader = request.headers.host[0].value;
    const redirectUri = 'https://' + hostHeader + '/auth/callback';

    const params = new URLSearchParams(request.querystring);
    const code = params.get('code');

    // Exchange code for tokens
    const tokenResponse = await fetch('https://' + COGNITO_DOMAIN + '.auth.' + COGNITO_REGION + '.amazoncognito.com/oauth2/token', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'Authorization': 'Basic ' + Buffer.from(COGNITO_CLIENT_ID+':'+COGNITO_CLIENT_SECRET).toString('base64')
        },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            code,
            redirect_uri: redirectUri,
        }),
    });

    const tokens = await tokenResponse.json();

    const response = {
        status: '302',
        statusDescription: 'Found',
        headers: {
            location: [{
                key: 'Location',
                value: '/'
            }],
            'set-cookie': [
                {
                    key: 'Set-Cookie',
                    value: `id_token=${tokens.id_token}; Path=/; Secure; HttpOnly; SameSite=Lax; Max-Age=3600`
                },
                {
                    key: 'Set-Cookie',
                    value: `access_token=${tokens.access_token}; Path=/; Secure; HttpOnly; SameSite=Lax; Max-Age=3600`
                },
            ],
        },
    };

    return response;
}

function handleAuthMe(request) {
    try {
        const headers = request.headers;
        const parsedCookies = {};

        // Parse cookies from the request
        if (headers.cookie && headers.cookie[0]) {
            const cookieString = headers.cookie[0].value;
            cookieString.split(';').forEach(cookie => {
                const parts = cookie.trim().split('=');
                if (parts.length === 2) {
                    parsedCookies[parts[0]] = parts[1];
                }
            });
        }

        const idToken = parsedCookies['id_token'];

        if (!idToken) {
            return {
                status: '401',
                statusDescription: 'Unauthorized',
                headers: {
                    'content-type': [{ key: 'Content-Type', value: 'application/json' }]
                },
                body: JSON.stringify({ error: 'No authentication token found' })
            };
        }

        // Decode JWT payload (base64 decode the middle part)
        const payload = JSON.parse(Buffer.from(idToken.split('.')[1], 'base64').toString());

        // Return user info in the format expected by the frontend
        const userInfo = {
            userId: payload.sub,
            username: payload['cognito:username'] || payload.sub,
            email: payload.email,
            name: payload.name,
            given_name: payload.given_name,
            family_name: payload.family_name,
            roles: payload['cognito:groups'] || ['authenticated']
        };

        return {
            status: '200',
            statusDescription: 'OK',
            headers: {
                'content-type': [{ key: 'Content-Type', value: 'application/json' }],
                'cache-control': [{ key: 'Cache-Control', value: 'no-cache' }]
            },
            body: JSON.stringify(userInfo)
        };

    } catch (error) {
        console.error('Error in handleAuthMe:', error);
        return {
            status: '500',
            statusDescription: 'Internal Server Error',
            headers: {
                'content-type': [{ key: 'Content-Type', value: 'application/json' }]
            },
            body: JSON.stringify({ error: 'Internal server error' })
        };
    }
}



