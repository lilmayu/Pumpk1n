package dev.mayuna.pumpk1n.api;

import dev.mayuna.pumpk1n.objects.DataHolder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Special abstract class for extending your own data-class implementation<br> Using this class, you can get this DataElement's parent DataHolder
 */
public abstract class ParentedDataElement implements DataElement {

    private transient @Getter DataHolder dataHolderParent;

    /**
     * Sets specified {@link DataHolder} as a DataElement's parent.<br>This method is used by {@link DataHolder}. <strong>You should not use this
     * method.</strong>
     *
     * @param dataHolderParent Non-null {@link DataHolder}
     */
    public void setDataHolderParent(@NonNull DataHolder dataHolderParent) {
        this.dataHolderParent = dataHolderParent;
    }
}
