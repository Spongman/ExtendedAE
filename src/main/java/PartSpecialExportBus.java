@Override
protected boolean doBusWork(IGrid grid) {
    var storageService = grid.getStorageService();
    var filter = this.createFilter();
    var context = createTransferContext(storageService, grid.getEnergyService());
    var exportStrategy = getExportStrategy();
    
    // Try to export by examining what the destination needs
    // rather than iterating through arbitrary storage order
    for (var what : storageService.getCachedInventory().keySet()) {
        if (!filter.isListed(what)) {
            continue;
        }
        
        var transferFactory = what.getAmountPerOperation();
        long amount = (long) context.getOperationsRemaining() * transferFactory;
        amount = exportStrategy.transfer(context, what, amount);
        
        // Only reduce operations if we actually exported something
        if (amount > 0) {
            context.reduceOperationsRemaining(Math.max(1, amount / transferFactory));
            if (!context.hasOperationsLeft()) {
                break;
            }
        }
    }
    
    return context.hasDoneWork();
}