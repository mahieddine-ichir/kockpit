import React from 'react';
import { DocumentTextIcon } from '@heroicons/react/24/outline';

const SidebarItem = ({ name, env, onClick, isActive }) => {
  return (
    <div 
      className={`flex items-center px-4 py-3 cursor-pointer transition-colors duration-200 ${
        isActive 
          ? 'bg-gray-900 text-white border-l-4 border-blue-500' 
          : 'text-gray-300 hover:bg-gray-700 hover:text-white'
      }`}
      onClick={onClick}
    >
      <DocumentTextIcon className={`h-5 w-5 mr-3 ${isActive ? 'text-blue-500' : 'text-gray-400'}`} />
      <div className="flex flex-col">
        <span className={`font-medium ${isActive ? 'text-white' : 'text-gray-300'}`}>{name}</span>
        <span className={`text-xs ${isActive ? 'text-gray-400' : 'text-gray-500'}`}>{env}</span>
      </div>
    </div>
  );
};

export default SidebarItem;