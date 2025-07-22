import { GoogleOAuthProvider, GoogleLogin } from '@react-oauth/google';
import { useAuth } from './AuthContext';
import { useNavigate } from 'react-router-dom';
import { ShieldCheckIcon } from '@heroicons/react/24/outline';

const LoginPage = () => {
    const { login } = useAuth();
    const navigate = useNavigate();

    return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
            <div className="w-full max-w-lg">
                <div className="bg-white rounded-xl shadow-md border-l-4 border-slate-800 overflow-hidden">
                    <div className="p-10">
                        <div className="flex flex-col items-center mb-8">
                            <h1 className="text-2xl font-semibold text-slate-800 mb-1">Welcome back</h1>
                            <p className="text-slate-500 text-base">Sign in to your account</p>
                        </div>
                        <div className="mb-6">
                            <GoogleOAuthProvider clientId="301835747273-k3gpt0civmb6dbpssb5f2bcae21u0fk6.apps.googleusercontent.com">
                                <GoogleLogin
                                    onSuccess={(credentialResponse) => {
                                        login(credentialResponse.credential);
                                        navigate('/audits');
                                    }}
                                    onError={() => console.log('Login Failed')}
                                    theme='filled_black'
                                    size="large"
                                    width="100%"
                                    shape="pill"
                                    text="signin_with"
                                    logo_alignment="left"
                                />
                            </GoogleOAuthProvider>
                        </div>
                        <div className="flex items-center mb-6">
                            <div className="flex-grow border-t border-slate-200"></div>
                            <span className="mx-4 text-xs text-slate-400 uppercase tracking-wider">or</span>
                            <div className="flex-grow border-t border-slate-200"></div>
                        </div>
                        <form className="space-y-5">
                            <div>
                                <label htmlFor="email" className="block text-sm font-medium text-slate-700 mb-1">Email Address</label>
                                <input
                                    id="email"
                                    name="email"
                                    type="email"
                                    autoComplete="email"
                                    required
                                    className="block w-full rounded-lg border border-slate-200 bg-white text-slate-800 placeholder-slate-400 focus:border-slate-800 focus:ring-2 focus:ring-slate-800 px-4 py-3 text-sm transition-all"
                                    placeholder="name@company.com"
                                />
                            </div>
                            <div>
                                <div className="flex items-center justify-between mb-1">
                                    <label htmlFor="password" className="block text-sm font-medium text-slate-700">Password</label>
                                    <a href="#" className="text-xs text-slate-600 hover:text-slate-800 transition-colors">Forgot password?</a>
                                </div>
                                <input
                                    id="password"
                                    name="password"
                                    type="password"
                                    autoComplete="current-password"
                                    required
                                    className="block w-full rounded-lg border border-slate-200 bg-white text-slate-800 placeholder-slate-400 focus:border-slate-800 focus:ring-2 focus:ring-slate-800 px-4 py-3 text-sm transition-all"
                                    placeholder="••••••••"
                                />
                            </div>
                            <div className="flex items-center">
                                <input
                                    id="remember-me"
                                    name="remember-me"
                                    type="checkbox"
                                    className="h-4 w-4 rounded border-slate-300 text-slate-800 focus:ring-slate-800"
                                />
                                <label htmlFor="remember-me" className="ml-2 block text-sm text-slate-600">Remember this device</label>
                            </div>
                            <div>
                                <button
                                    type="submit"
                                    className="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg text-sm font-medium text-white bg-slate-800 hover:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-800 focus:ring-offset-1 transition-all shadow-sm hover:shadow-md text-base"
                                >
                                    Sign In
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;