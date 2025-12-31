import React from 'react';

const StatusBadge = ({ status }) => {
  const getColor = () => {
    if (status >= 500) return 'bg-red-100 text-red-800';
    if (status >= 400) return 'bg-orange-100 text-orange-800';
    if (status >= 300) return 'bg-blue-100 text-blue-800';
    return 'bg-green-100 text-green-800';
  };

  return (
    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getColor()}`}>
      {status}
    </span>
  );
};

export default StatusBadge;