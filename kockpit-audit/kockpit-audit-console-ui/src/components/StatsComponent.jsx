import React from "react";

const Stats = ({totalKeys, modifiedKeys, keys}) => {
    return (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
            <div className="bg-white rounded-lg border border-gray-200 p-6">
                <div className="text-sm text-gray-600 font-medium mb-2">Total</div>
                <div className="text-4xl font-bold text-gray-900">{totalKeys}</div>
                <div className="text-xs text-gray-500 mt-2">clés configurées</div>
            </div>

            <div className="bg-green-50 rounded-lg border border-green-200 p-6">
                <div className="text-sm text-green-700 font-medium mb-2">✓ Syncronisées</div>
                <div className="text-4xl font-bold text-green-700">{totalKeys - modifiedKeys}</div>
                <div className="text-xs text-green-600 mt-2">sans modification</div>
            </div>

            <div className="bg-yellow-50 rounded-lg border border-yellow-200 p-6">
                <div className="text-sm text-yellow-700 font-medium mb-2">⚠ Modifiées</div>
                <div className="text-4xl font-bold text-yellow-700">{modifiedKeys}</div>
                <div className="text-xs text-yellow-600 mt-2">en attente de sauvegarde</div>
            </div>

            <div className="bg-blue-50 rounded-lg border border-blue-200 p-6">
                <div className="text-sm text-blue-700 font-medium mb-2">≠ Configurations</div>
                <div className="text-4xl font-bold text-blue-700">{keys.length}</div>
                <div className="text-xs text-blue-600 mt-2">groupes actifs</div>
            </div>
        </div>
    )
}

export default Stats;
