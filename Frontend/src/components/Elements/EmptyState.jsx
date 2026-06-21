import { PackageOpen } from 'lucide-react';

const EmptyState = ({ icon: Icon = PackageOpen, title = 'Không có dữ liệu', description = '', action }) => {
    return (
        <div className="flex flex-col items-center justify-center py-16 px-6 text-center">
            <div className="w-20 h-20 rounded-2xl bg-slate-100 flex items-center justify-center mb-6">
                <Icon className="w-10 h-10 text-slate-300" />
            </div>
            <h3 className="text-lg font-bold text-slate-700 mb-2">{title}</h3>
            {description && <p className="text-sm text-slate-500 max-w-sm leading-relaxed mb-6">{description}</p>}
            {action && action}
        </div>
    );
};

export default EmptyState;
