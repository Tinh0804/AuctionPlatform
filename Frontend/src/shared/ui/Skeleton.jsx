const Skeleton = ({ className = '', variant = 'rectangle' }) => {
    const baseClass = 'animate-pulse bg-gradient-to-r from-slate-200 via-slate-100 to-slate-200 bg-[length:200%_100%]';
    
    const variants = {
        rectangle: 'rounded-xl',
        circle: 'rounded-full',
        text: 'rounded-md h-4',
        card: 'rounded-2xl',
    };

    return <div className={`${baseClass} ${variants[variant] || variants.rectangle} ${className}`} />;
};

// Pre-built skeleton patterns
Skeleton.Card = () => (
    <div className="card-luxury overflow-hidden">
        <Skeleton className="aspect-[4/3] rounded-none" />
        <div className="p-5 space-y-3">
            <Skeleton variant="text" className="w-3/4 h-5" />
            <Skeleton variant="text" className="w-1/2 h-4" />
            <div className="pt-4 border-t border-slate-100 flex justify-between items-center">
                <div className="space-y-2">
                    <Skeleton variant="text" className="w-20 h-3" />
                    <Skeleton variant="text" className="w-28 h-6" />
                </div>
                <Skeleton variant="text" className="w-16 h-4" />
            </div>
        </div>
    </div>
);

Skeleton.Detail = () => (
    <div className="max-w-7xl mx-auto px-6 py-10">
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-10">
            <div className="lg:col-span-3">
                <Skeleton className="aspect-[4/3] rounded-2xl" />
            </div>
            <div className="lg:col-span-2 space-y-4">
                <Skeleton variant="text" className="w-24 h-8" />
                <Skeleton variant="text" className="w-full h-8" />
                <Skeleton variant="text" className="w-2/3 h-6" />
                <div className="space-y-3 pt-6">
                    <Skeleton className="w-full h-20 rounded-xl" />
                    <Skeleton className="w-full h-14 rounded-xl" />
                    <Skeleton className="w-full h-14 rounded-xl" />
                </div>
            </div>
        </div>
    </div>
);

Skeleton.List = ({ count = 6 }) => (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {Array.from({ length: count }).map((_, i) => (
            <Skeleton.Card key={i} />
        ))}
    </div>
);

export default Skeleton;
