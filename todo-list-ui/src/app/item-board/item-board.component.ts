import {ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {ItemService} from '../service/item.service';
import {finalize, take} from 'rxjs/operators';
import {ItemStatus} from '../model/item-status.enum';
import {MatDialog} from '@angular/material/dialog';
import {CdkDragDrop, transferArrayItem} from '@angular/cdk/drag-drop';
import {ItemSaveDialogComponent} from '../item-save-dialog/item-save-dialog.component';
import {MapUtils} from '../utils/map-utils';
import {Subscription} from 'rxjs';
import {Item} from '../model/item';
import {EventMessage} from '../model/event-message';

@Component({
  selector: 'app-item-board',
  templateUrl: './item-board.component.html',
  styleUrls: ['./item-board.component.scss'],
})
export class ItemBoardComponent implements OnInit, OnDestroy {

  private $eventObservable: Subscription;

  ItemStatus = ItemStatus;
  statusItemsMap = new Map<string, Item[]>();
  actionInProgress = false;
  dragAndDropInProgress = false;

  constructor(private readonly itemService: ItemService,
              private readonly changeDetector: ChangeDetectorRef,
              private readonly dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.refresh();

    // Listen to all changes
    this.$eventObservable = this.itemService.listenToEvents().subscribe((itemEvent) => {
      this.handleEvent(itemEvent);
    });
  }

  ngOnDestroy(): void {
    this.$eventObservable.unsubscribe();
  }

  refresh() {

    this.actionInProgress = true;

    for (const status of this.getStatuses()) {
      this.statusItemsMap.set(status, []);
    }

    this.itemService.findAll()
      .pipe(finalize(() => {
        this.stopActionInProgress();
      }))
      .subscribe(item => {
        this.statusItemsMap.get(item.status).push(item);
      });
  }

  getStatuses(): Array<string> {
    return Object.keys(ItemStatus);
  }

  add() {
    this.startActionInProgress();

    const dialogRef = this.dialog.open(ItemSaveDialogComponent, {
      width: '650px',
      disableClose: true
    });

    dialogRef.afterClosed().pipe(take(1)).subscribe((response) => {
      if (response) {
        this.itemService.addItem(response)
          .pipe(take(1), finalize(() => this.stopActionInProgress()))
          .subscribe(() => {
          });
      } else {
        this.stopActionInProgress();
      }
    });
  }

  drop(event: CdkDragDrop<any, any>) {
    if (event.previousContainer !== event.container) {

      this.startActionInProgress();

      // New status
      const newStatus = MapUtils.findKeyByValue(this.statusItemsMap, event.container.data);

      // Moved item
      const item = event.previousContainer.data[event.previousIndex];

      transferArrayItem(event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex);

      // Update the status of the dropped item
      this.itemService.updateStatus(item.id, item.version, newStatus)
        .pipe(take(1), finalize(() => this.stopActionInProgress()))
        .subscribe(() => {
        });
    }
  }

  startActionInProgress() {
    this.actionInProgress = true;
  }

  stopActionInProgress() {
    this.actionInProgress = false;
    this.changeDetector.detectChanges();
  }

  startDragAction() {
    this.dragAndDropInProgress = true;
  }

  stopDragAction() {
    this.dragAndDropInProgress = false;
  }

  private handleEvent(eventWrapper: EventMessage) {

    const receivedEvent = eventWrapper.event;

    switch (eventWrapper.eventType) {
      case 'ItemDeleted':
        this.removeItem(receivedEvent.itemId);
        break;

      case 'ItemSaved':
        const item = receivedEvent.item;
        this.removeItem(item.id);
        // Add it to the correct status column
        this.statusItemsMap.get(item.status).push(item);
        break;

      case 'PingEvent':
        console.log('Ping received from sever');
        break;

      default:
        console.error('Unsupported event received: ' + receivedEvent);
    }
  }

  private removeItem(itemId: string) {
    // Remove the item
    for (const items of this.statusItemsMap.values()) {
      const index = items.map(i => i.id).indexOf(itemId);
      if (index >= 0) {
        items.splice(index, 1);
      }
    }
  }
}
