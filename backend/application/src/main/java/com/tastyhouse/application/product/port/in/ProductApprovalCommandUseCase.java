package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface ProductApprovalCommandUseCase {

    void approveImageChange(ProductImageChangeApproveCommand command);

    void rejectImageChange(ProductImageChangeRejectCommand command);

    void approveVegetarian(ProductVegetarianApproveCommand command);

    void rejectVegetarian(ProductVegetarianRejectCommand command);

    void approveRepresentative(ProductRepresentativeApproveCommand command);

    void rejectRepresentative(ProductRepresentativeRejectCommand command);
}
