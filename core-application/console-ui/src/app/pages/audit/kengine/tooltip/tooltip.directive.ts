import {ComponentRef, Directive, ElementRef, HostListener, Input, OnInit} from '@angular/core';
import {Overlay, OverlayPositionBuilder, OverlayRef} from '@angular/cdk/overlay';
import {TooltipComponent} from './tooltip.component';
import {ComponentPortal} from '@angular/cdk/portal';

@Directive({
  selector: '[wccTooltip]'
})
export class TooltipDirective implements OnInit {

  // tslint:disable-next-line:no-input-rename
  @Input('tooltip') text = '';
  // tslint:disable-next-line:no-input-rename
  @Input('image') image = '';
  private overlayRef: OverlayRef;

  constructor(private overlay: Overlay,
              private overlayPositionBuilder: OverlayPositionBuilder,
              private elementRef: ElementRef) { }

  ngOnInit(): void {
    const positionStrategy = this.overlayPositionBuilder
      .flexibleConnectedTo(this.elementRef)
      .withPositions([{
        originX: 'center',
        originY: 'top',
        overlayX: 'center',
        overlayY: 'bottom',
        offsetY: -8,
      }]);

    this.overlayRef = this.overlay.create({ positionStrategy });

  }

  @HostListener('mouseenter')
  show() {
    const tooltipRef: ComponentRef<TooltipComponent>
      = this.overlayRef.attach(new ComponentPortal(TooltipComponent));
    tooltipRef.instance.text = this.text;
    tooltipRef.instance.image = this.image;
  }

  @HostListener('mouseout')
  hide() {
    this.overlayRef.detach();
  }

}
