import {Injectable, NgZone} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Item} from '../model/item';
import {Observable} from 'rxjs';
import {ItemStatus} from '../model/item-status.enum';
import {environment} from '../../environments/environment';
import {take} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ItemService {

  readonly baseUrl;

  constructor(private readonly http: HttpClient, private readonly ngZone: NgZone) {
    this.baseUrl = `${environment.apiUrl}/items`;
  }

  private static buildOptions(version: number) {
    return {
      headers: new HttpHeaders({
        'if-match': String(version)
      })
    };
  }

  findAll(): Observable<Item[]> {
    return this.http.get<Item[]>(this.baseUrl).pipe(take(1));
  }

  addItem(description: string): Observable<any> {
    return this.http.post<Item>(this.baseUrl, {description})
      .pipe(take(1));
  }

  findById(id: string): Observable<Item> {
    return this.http.get<Item>(`${this.baseUrl}/${id}`)
      .pipe(take(1));
  }

  delete(id: string, version: number): Observable<any> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`)
      .pipe(take(1));
  }

  updateDescription(id: string, version: number, description: string): Observable<any> {
    return this.http.patch<void>(`${this.baseUrl}/${id}`, {description})
      .pipe(take(1));
  }

  updateStatus(id: string, version: number, status: ItemStatus): Observable<any> {
    return this.http.patch<void>(`${this.baseUrl}/${id}`, {status})
      .pipe(take(1));
  }

}
