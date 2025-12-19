import React from 'react';
import {useNavigate} from 'react-router-dom';
import {
    ArrowLeftStartOnRectangleIcon,
    ChevronDoubleLeftIcon,
    ChevronDoubleRightIcon,
    ChevronDownIcon,
    ChevronRightIcon,
    CogIcon,
    DocumentTextIcon,
    HomeIcon,
    UserIcon
} from '@heroicons/react/24/outline';
import {logout} from "../services/api.js";

const UserInfo = ({collapsed, currentUser, logout}) => {
    return (
        <div className="flex items-center space-x-3">
            <div className="h-11 w-11 rounded-full bg-gradient-to-br from-blue-600 to-blue-400 flex items-center justify-center shadow-lg border-2 border-blue-500">
                <UserIcon className="h-6 w-6 text-white" />
            </div>
            {collapsed ? null :
                        <div className="flex-1 min-w-0">
                            <p className="text-base font-semibold text-white truncate">{currentUser}</p>
                        </div>
            }
            {currentUser ?
                <div className="flex items-center space-x-2" onClick={() => logout()}>
                    <ArrowLeftStartOnRectangleIcon className="h-6 w-6 text-white" />
                </div> : null
            }

        </div>
    )
}

let asLabel = (arg) => {
    if (!arg) {
        return '';
    }
    let label = arg[0].toUpperCase();
    for (let i = 1; i < arg.length; i++) {
        if (arg[i].match(/[\\-]/) != null) {
            label += ' ';
            i++;
            label += arg[i].toUpperCase();
        } else {
            label += arg[i];
        }
    }
    return label.trim();
};

const Sidebar = ({ collapsed, setCollapsed, config, user }) => {
  const navigate = useNavigate();
  const currentUser = user?.["clientPrincipal"]?.["userDetails"];
  console.log(`currentUser ${currentUser}`)
  let navItems = [];
  if (config['services']) {
      config['services']
          .forEach(service => {
              if (navItems.find(item => item.type === service.type) == null)
                  navItems.push({
                      type: service.type,
                      label: service.label ? service.label : asLabel(service.type),
                      subMenus: []
                  })

              navItems.find(item => item.type === service.type).subMenus.push({
                  name: service.name,
                  type: service.type,
                  id: service.id,
                  label: service.label ? service.label : asLabel(service.name)
              })
          });
  }

  const logoutUser = () => {
      logout().then(() => {
         navigate("/login");
      });
  };

  return (
      <div className={`bg-gradient-to-b from-slate-900 via-slate-800 to-slate-700 text-white h-screen fixed flex flex-col transition-all duration-300 ease-in-out shadow-2xl rounded-r-2xl ${collapsed ? 'w-20' : 'w-64'}`} style={{ minWidth: collapsed ? '5rem' : '16rem' }}>
        <div className="flex items-center justify-between p-4 border-b border-slate-700">
          {!collapsed && (
              <div className="flex items-center space-x-2">
                <img src="/kockpit.svg" alt="Kockpit" className="h-8" />
              </div>
          )}
          <button
              onClick={() => setCollapsed(c => !c)}
              className="p-2 rounded-lg hover:bg-slate-700 focus:outline-none focus:ring-2 focus:ring-blue-400 transition-all"
              aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          >
            {collapsed ? (
                <ChevronDoubleRightIcon className="h-6 w-6 text-slate-300" />
            ) : (
                <ChevronDoubleLeftIcon className="h-6 w-6 text-slate-300" />
            )}
          </button>
        </div>

          <div className="flex-1 overflow-y-auto py-4">
              <h3 className={`px-6 py-2 text-xs font-semibold text-slate-400 uppercase tracking-wider sticky top-0 bg-slate-800/90 z-10 ${collapsed ? 'hidden' : ''}`}>Navigation</h3>
              <nav className="space-y-1 px-2">
                  <NavItem
                      icon={<HomeIcon className="h-5 w-5" />}
                      label="Home"
                      collapsed={collapsed}
                      onClick={() => navigate('/home')}
                      active={location.pathname === '/home'}
                  />
                  {
                      navItems.map((navItem) => {
                          return (
                              <NavItem
                                  icon={<DocumentTextIcon className="h-5 w-5" />}
                                  label={navItem.label}
                                  key={navItem.id}
                                  collapsed={collapsed}
                                  active={location.pathname === `/${navItem.type}`}
                                  subItems={navItem.subMenus}
                                  parent={navItem}
                              />
                          )
                      })
                  }
                  <NavItem
                      icon={<CogIcon className="h-5 w-5" />}
                      label="Instances health"
                      collapsed={collapsed}
                      onClick={() => navigate('/health')}
                      active={location.pathname === '/health'}
                  />
                  <NavItem
                      icon={<CogIcon className="h-5 w-5" />}
                      label="Config"
                      collapsed={collapsed}
                      onClick={() => navigate('/config')}
                      active={location.pathname === '/config'}
                  />
              </nav>
          </div>
        <div className="border-t border-slate-700 bg-slate-800/70">
            {!collapsed && (
                <div className="px-4 pt-2 pb-1">
                    <p className="text-xs text-slate-400">
                        Build: {new Date(__BUILD_TIME__).toLocaleString()}
                    </p>
                </div>
            )}
            <div className="p-4">
                <UserInfo collapsed={collapsed} currentUser={currentUser} logout={logoutUser} />
            </div>
        </div>
      </div>
  );
};

const NavItem = ({ icon, label, collapsed, onClick, active = false, subItems = [] }) => {
    const navigate = useNavigate();
  const [isExpanded, setIsExpanded] = React.useState(true);
  const hasSubItems = subItems.length > 0;

  const handleClick = () => {
    if (hasSubItems) {
      setIsExpanded(!isExpanded);
    } else if (onClick) {
      onClick();
    }
  };

  return (
    <>
      <div
        onClick={handleClick}
        className={`flex items-center px-3 py-2.5 rounded-lg cursor-pointer transition-all duration-200 group relative
          ${active ? 'bg-blue-600/20 text-blue-400 border-l-4 border-blue-500 shadow-md' : 'text-slate-300 hover:bg-slate-700 hover:text-white border-l-4 border-transparent'}
          ${collapsed ? 'justify-center px-0' : ''}`}
        style={{ minHeight: '44px' }}
      >
        <div className="flex items-center flex-1">
          {icon}
          {!collapsed && <span className="ml-3 text-sm font-medium">{label}</span>}
        </div>
        {hasSubItems && !collapsed && (
          <div className="ml-auto">
            {isExpanded ? (
              <ChevronDownIcon className="h-4 w-4" />
            ) : (
              <ChevronRightIcon className="h-4 w-4" />
            )}
          </div>
        )}
        {active && !collapsed && (
          <span className="absolute left-0 top-0 h-full w-1 bg-blue-500 rounded-r"></span>
        )}
      </div>
      {hasSubItems && isExpanded && !collapsed && (
        <div className="ml-4 space-y-1">
          {subItems.map((subItem, index) => (
            <div
              key={index}
              onClick={onClick ? onClick : () => navigate(`${subItem.type}?service=${subItem.id}`)}
              className={`flex items-center px-3 py-2 rounded-lg cursor-pointer transition-all duration-200
                ${subItem.active ? 'bg-blue-600/10 text-blue-400' : 'text-slate-400 hover:bg-slate-700 hover:text-white'}`}
            >
              <span className="text-sm">{subItem.label}</span>
            </div>
          ))}
        </div>
      )}
    </>
  );
};

export default Sidebar;
