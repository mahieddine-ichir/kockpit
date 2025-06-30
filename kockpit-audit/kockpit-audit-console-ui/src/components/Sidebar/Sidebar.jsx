import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Bars3Icon, ChevronDoubleLeftIcon, ChevronDoubleRightIcon} from '@heroicons/react/24/outline';

const Sidebar = ({ collapsed, setCollapsed }) => {
  const navigate = useNavigate();
  const [userInfo, setUserInfo] = useState({});
  const redirectUri = window.location.pathname;

  useEffect(() => {
      (async () => {
          setUserInfo(await getUserInfo());
      })();
  }, []);

    async function getUserInfo() {
        const response = await fetch('/.auth/me');
        const payload = await response.json();
        const { clientPrincipal } = payload;
        return clientPrincipal;
    }

  return (
    <div className={`bg-gray-800 text-white h-screen fixed flex flex-col transition-all duration-300 ${collapsed ? 'w-16' : 'w-64'}`}>
      <div className="flex items-center justify-between p-4 text-xl font-bold border-b border-gray-700">
          {!collapsed &&
              <span>
                  <img src="/kockpit.svg"  alt="Kockpit"/>
              </span>
          }
        <button
          onClick={() => setCollapsed(c => !c)}
          className={`p-1 absolute ${
              collapsed ? 'left-0' : 'right-0'
          } top-1/2 transform -translate-y-1/2 bg-gray-800 rounded hover:bg-gray-700 focus:outline-none z-10`}
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {collapsed ? (
            <ChevronDoubleRightIcon className="h-6 w-6 text-white" />
          ) : (
            <ChevronDoubleLeftIcon className="h-6 w-6 text-white" />
          )}
        </button>
      </div>

      <div className="flex-1 overflow-y-auto">
        <h3 className={`px-4 py-3 text-sm font-semibold text-gray-400 sticky top-0 bg-gray-800 z-10 ${collapsed ? 'hidden' : ''}`}>Navigation</h3>
          <nav className="pb-4">
              {userInfo ?
                <a key="aad" href={`/.auth/login/aad?post_login_redirect_uri=${redirectUri}`}>
                    Login
                </a> : null
              }
          </nav>

        <nav className="pb-4">
          <div
            className={`flex items-center px-4 py-3 cursor-pointer transition-colors duration-200 mx-2 rounded text-gray-300 hover:bg-gray-700 hover:text-white ${collapsed ? 'justify-center px-0' : ''}`}
            onClick={() => navigate('/')}
          >
            <Bars3Icon className="h-6 w-6 mr-0.5" />
            {!collapsed && <span className="font-medium truncate ml-2">Audits</span>}
          </div>
        </nav>
      </div>
    </div>
  );
};

export default Sidebar;