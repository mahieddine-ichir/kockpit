import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { exchangeCodeForTokens } from '../services/api.js';

function CallbackPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const handleCallback = async () => {
            try {
                // Get URL parameters
                const urlParams = new URLSearchParams(window.location.search);
                const authCode = urlParams.get('code');
                const errorParam = urlParams.get('error');
                const errorDescription = urlParams.get('error_description');

                if (errorParam) {
                    throw new Error(errorDescription || errorParam);
                }

                if (!authCode) {
                    throw new Error('No authorization code received');
                }

                console.log('Processing OAuth callback with code:', authCode);

                // Exchange code for tokens
                const result = await exchangeCodeForTokens(authCode);

                if (result && result.user) {
                    // Store user info in localStorage for the main app
                    localStorage.setItem('cognito_user', JSON.stringify(result.user));

                    // Redirect to home page
                    navigate('/home');
                } else {
                    throw new Error('Failed to get user information from token exchange');
                }

            } catch (err) {
                console.error('OAuth callback error:', err);
                setError(err.message);
                setLoading(false);
            }
        };

        // Call async function and handle any unhandled promise rejections
        handleCallback().catch((err) => {
            console.error('Unhandled error in handleCallback:', err);
            setError('An unexpected error occurred during authentication');
            setLoading(false);
        });
    }, [navigate]);

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-gray-600">Processing authentication...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <div className="text-center max-w-md p-6 bg-red-50 rounded-lg border border-red-200">
                    <h2 className="text-xl font-semibold text-red-800 mb-2">Authentication Error</h2>
                    <p className="text-red-600 mb-4">{error}</p>
                    <button
                        onClick={() => window.location.href = '/'}
                        className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                    >
                        Return to Home
                    </button>
                </div>
            </div>
        );
    }

    return null;
}

export default CallbackPage;