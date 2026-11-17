package org.delcom.ifs25002springbootcashflow.framework.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ImageZoomDialog extends Dialog {

    public ImageZoomDialog(String imageUrl, String altText) {
        setHeaderTitle("Pratinjau Lampiran");

        Image previewImage = new Image(imageUrl, altText);
        previewImage.setMaxWidth("80vw");
        previewImage.setMaxHeight("70vh");
        previewImage.getStyle()
                .set("object-fit", "contain")
                .set("border-radius", "10px");

        VerticalLayout container = new VerticalLayout(previewImage);
        container.setPadding(false);
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        add(container);

        Button dismissButton = new Button("Tutup", event -> close());
        dismissButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getFooter().add(dismissButton);
    }
}