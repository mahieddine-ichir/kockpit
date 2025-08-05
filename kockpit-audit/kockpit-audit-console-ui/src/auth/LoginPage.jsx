import {useAuth} from './AuthContext';
import {useNavigate} from 'react-router-dom';
import {authenticate} from "../services/api.js";
import {useState} from "react";

const LoginPage = () => {
    const { login } = useAuth();
    const navigate = useNavigate();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');

    function handleLogin() {
        console.log("authenticating ... ")
        authenticate(username, password).then(value => {
            localStorage.setItem('creds', btoa(`${username}:${password}`));
            login(value);
            navigate('/audits');
        }).catch(error => {
            setError(error);
        })
    }

    return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
            <div className="w-full max-w-lg">
                <div className="bg-white rounded-xl shadow-md border-l-4 border-slate-800 overflow-hidden">
                    <div className="p-10">
                        <div className="flex flex-col items-center mb-8">
                            <h1 className="text-2xl font-semibold text-slate-800 mb-1">Kockpit</h1>
                            <p className="text-slate-500 text-base">Sign in to your account</p>
                        </div>
                        <div className="space-y-5">
                            <div>
                                <label htmlFor="email" className="block text-sm font-medium text-slate-700 mb-1">Login</label>
                                <input
                                    id="username"
                                    name="username"
                                    type="text"
                                    onChange={(e) => setUsername(e.target.value)}
                                    required
                                    className="block w-full rounded-lg border border-slate-200 bg-white text-slate-800 placeholder-slate-400 focus:border-slate-800 focus:ring-2 focus:ring-slate-800 px-4 py-3 text-sm transition-all"
                                    placeholder="username"
                                />
                            </div>
                            <div>
                                <input
                                    id="password"
                                    name="password"
                                    type="password"
                                    onChange={(e) => setPassword(e.target.value)}
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
                                    className="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg text-sm font-medium text-white bg-slate-800 hover:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-slate-800 focus:ring-offset-1 transition-all shadow-sm hover:shadow-md text-base"
                                    onClick={handleLogin}
                                >
                                    Sign In
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;