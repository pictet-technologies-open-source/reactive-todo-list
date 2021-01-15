import {Injectable, NgZone} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Item} from '../model/item';
import {Observable, Subscriber} from 'rxjs';
import {ItemStatus} from '../model/item-status.enum';
import {environment} from '../../environments/environment';
import {EventMessage} from '../model/event-message';
import {take} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class ItemService {

  readonly baseUrl;

  constructor(private readonly http: HttpClient, private readonly ngZone: NgZone) {
    this.baseUrl = `${environment.apiUrl}/items`;
  }

  private static buildOptionsIfMatch(version: number) {
    return {
      headers: new HttpHeaders({
        'if-match': String(version)
      })
    };
  }

  findAll(): Observable<Item> {
    return new Observable<Item>((observer) => {
      this.handleMessageEvent(new EventSource(this.baseUrl ), observer);
    });
  }

  listenToEvents(): Observable<EventMessage> {
    return new Observable<EventMessage>((observer) => {

      this.handleMessageEvent(new EventSource(`${this.baseUrl}/events`), observer, true);
    });
  }

  addItem(description: string): Observable<Item[]> {
    return this.http.post<Item[]>(this.baseUrl, {description})
      .pipe(take(1));
  }

  findById(id: string): Observable<Item> {
    return this.http.get<Item>(`${this.baseUrl}/${id}`)
      .pipe(take(1));
  }

  delete(id: string, version: number): Observable<any> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, ItemService.buildOptionsIfMatch(version))
      .pipe(take(1));
  }

  updateDescription(id: string, version: number, description: string) {
    return this.http.patch<void>(`${this.baseUrl}/${id}`, {description}, ItemService.buildOptionsIfMatch(version))
      .pipe(take(1));
  }

  updateStatus(id: string, version: number, status: ItemStatus) {
    return this.http.patch<void>(`${this.baseUrl}/${id}`, {status}, ItemService.buildOptionsIfMatch(version))
      .pipe(take(1));
  }

  private handleMessageEvent(eventSource: EventSource, observer: Subscriber<any>, keepAlive = false) {
    eventSource.onmessage = (event) => {
      const item = JSON.parse(event.data);
      this.ngZone.run(() => observer.next(item));
    };

    eventSource.onerror = (error) => {

      if (eventSource.readyState === 0) {

        const message = 'Stream closed';
        if (! keepAlive) {
          eventSource.close();
          observer.complete();
        } else {
          console.error(message);
        }
      } else {
        observer.error(error);
      }
    };
  }
}
