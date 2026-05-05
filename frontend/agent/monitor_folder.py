#!/usr/bin/env python3
"""
Script pentru monitorizarea unui folder si analiza automata a PDF-urilor noi.
"""

import sys
import os
import time
import re
import json
import base64
from pathlib import Path
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from prompt import check_achizitii_intracomunitare
import requests

class PDFHandler(FileSystemEventHandler):
    """Handler pentru evenimente de fisiere PDF."""
    
    def __init__(self, api_key=None, max_pages=10, api_url=None, api_user=None, api_pass=None):
        self.api_key = api_key
        self.max_pages = max_pages
        self.processed_files = set()
        self.api_url = api_url or os.getenv('API_URL') or "http://localhost:3000/api/tasks/generateConditional"
        self.api_user = api_user
        self.api_pass = api_pass
    
    def on_created(self, event):
        """Apelat cand un fisier nou este creat."""
        if event.is_directory:
            return
        
        file_path = event.src_path
        
        # Verifica daca este PDF
        if not file_path.lower().endswith('.pdf'):
            return
        
        # Verifica daca nu a fost deja procesat
        if file_path in self.processed_files:
            return
        
        # Asteapta putin pentru a se asigura ca fisierul s-a terminat de copiat
        time.sleep(2)
        
        # Verifica daca fisierul exista si poate fi citit
        if not os.path.exists(file_path):
            return
        
        print(f"\n{'='*80}")
        print(f"PDF NOU DETECTAT: {file_path}")
        print(f"{'='*80}")
        
        # Marcheaza ca procesat
        self.processed_files.add(file_path)
        
        # Analizeaza PDF-ul
        self.analyze_pdf(file_path)
    
    def extract_path_info(self, pdf_path):
        """
        Extrage clientId, year, month din calea fisierului.
        Format asteptat: <root>/<clientId>/<year>/<month>/file.pdf
        
        Returns:
            tuple: (clientId, year, month) sau (None, None, None) daca nu se potriveste
        """
        try:
            path_parts = Path(pdf_path).parts
            # Cauta pattern: .../<numar>/<an>/<luna>/file.pdf
            if len(path_parts) >= 4:
                month = path_parts[-2]  #Folder-ul inainte de fisier
                year = path_parts[-3]   # Folder-ul inainte de luna
                client_id = path_parts[-4]  # Folder-ul inainte de an
                
                # Valideaza ca sunt numere
                if client_id.isdigit() and year.isdigit() and month.isdigit():
                    client_id_int = int(client_id)
                    year_int = int(year)
                    month_int = int(month)
                    
                    # Valideaza luna si anul
                    if 1 <= month_int <= 12 and 1900 <= year_int <= 2100:
                        return (client_id_int, year_int, month_int)
            
            return (None, None, None)
        except Exception as e:
            print(f"⚠️  Eroare la extragerea info din cale: {str(e)}")
            return (None, None, None)
    
    def call_api(self, client_id, year, month, note="390"):
        """
        Apeleaza API-ul pentru a crea/actualiza task-ul.
        
        Args:
            client_id: ID-ul clientului
            year: Anul
            month: Luna
            note: Nota de adaugat (default: "390")
        
        Returns:
            dict: Raspunsul API-ului sau None in caz de eroare
        """
        try:
            if not self.api_user or not self.api_pass:
                print("⚠️  API credentials nu sunt setate. Skip API call.")
                return None
            
            url = f"{self.api_url}?clientId={client_id}&month={month}&year={year}&note={note}"
            
            print(f"\n📡 Apelez API: {url}")
            
            # Basic Auth
            auth = (self.api_user, self.api_pass)
            
            response = requests.post(url, auth=auth, timeout=30)
            
            if response.status_code == 200:
                result = response.json()
                print("✅ API call reusit!")
                print(f"   Rezultat: {json.dumps(result, indent=2)}")
                return result
            else:
                print(f"❌ API call esuat: {response.status_code}")
                print(f"   Raspuns: {response.text}")
                return None
                
        except Exception as e:
            print(f"❌ Eroare la apelul API: {str(e)}")
            return None
    
    def analyze_pdf(self, pdf_path):
        """Analizeaza un PDF."""
        try:
            print(f"\nAnalizez fisierul: {pdf_path}")
            print("Se convertesc paginile in imagini...")
            
            result = check_achizitii_intracomunitare(pdf_path, self.api_key, self.max_pages)
            
            if result.get("success"):
                print("\n" + "="*80)
                print("REZULTAT ANALIZA")
                print("="*80)
                print(result["raspuns"])
                print("\n" + "="*80)
                print(f"Tokens folositi: {result.get('tokens_used', 'N/A')}")
                print(f"Pagini procesate: {result.get('pages_processed', 'N/A')}")
                
                # Salveaza rezultatul intr-un fisier text
                result_file = pdf_path.replace('.pdf', '_rezultat.txt')
                with open(result_file, 'w', encoding='utf-8') as f:
                    f.write(f"Analiza pentru: {pdf_path}\n")
                    f.write("="*80 + "\n\n")
                    f.write(result["raspuns"])
                    f.write(f"\n\nTokens folositi: {result.get('tokens_used', 'N/A')}")
                    f.write(f"\nPagini procesate: {result.get('pages_processed', 'N/A')}")
                
                print(f"\nRezultatul a fost salvat in: {result_file}")
                
                # Verifica daca sunt achizitii intracomunitare si apeleaza API-ul
                raspuns_lower = result["raspuns"].lower()
                has_achizitii = False
                
                # Cauta indicii ca exista achizitii intracomunitare
                if any(keyword in raspuns_lower for keyword in [
                    '"are_achizitii_intracomunitare": true',
                    '"are_achizitii_intracomunitare":true',
                    'are achizitii intracomunitare',
                    'achizitii intracomunitare detectate',
                    'dovezi de achizitii intracomunitare'
                ]):
                    has_achizitii = True
                
                if has_achizitii:
                    print("\n🔔 Achizitii intracomunitare detectate!")
                    
                    # Extrage info din cale
                    client_id, year, month = self.extract_path_info(pdf_path)
                    
                    if client_id and year and month:
                        print(f"📋 Informatii din cale: clientId={client_id}, year={year}, month={month}")
                        
                        # Apeleaza API-ul
                        api_result = self.call_api(client_id, year, month, note="390")
                        
                        if api_result:
                            # Salveaza si rezultatul API-ului
                            api_result_file = pdf_path.replace('.pdf', '_api_rezultat.json')
                            with open(api_result_file, 'w', encoding='utf-8') as f:
                                json.dump(api_result, f, indent=2, ensure_ascii=False)
                            print(f"💾 Rezultatul API salvat in: {api_result_file}")
                    else:
                        print("⚠️  Nu am putut extrage clientId, year, month din calea fisierului.")
                        print(f"   Format asteptat: <root>/<clientId>/<year>/<month>/file.pdf")
                        print(f"   Cale primita: {pdf_path}")
                else:
                    print("\nℹ️  Nu s-au detectat achizitii intracomunitare.")
            else:
                print(f"\n❌ Eroare la analiza: {result.get('error')}")
        
        except Exception as e:
            print(f"\n❌ Eroare la procesarea fisierului: {str(e)}")

def monitor_folder(folder_path, api_key=None, max_pages=10, api_url=None, api_user=None, api_pass=None):
    """
    Monitorizeaza un folder pentru PDF-uri noi.
    
    Args:
        folder_path: Calea catre folderul de monitorizat
        api_key: OpenAI API key (optional)
        max_pages: Numarul maxim de pagini de procesat per PDF
        api_url: URL-ul API-ului de task-uri (optional)
        api_user: Username pentru API (optional)
        api_pass: Parola pentru API (optional)
    """
    # Verifica daca folderul exista
    if not os.path.exists(folder_path):
        print(f"❌ Folderul {folder_path} nu exista!")
        return
    
    if not os.path.isdir(folder_path):
        print(f"❌ {folder_path} nu este un folder!")
        return
    
    print(f"📁 Monitorizez folderul: {folder_path}")
    print(f"📄 Voi procesa pana la {max_pages} pagini per PDF")
    if api_user and api_pass:
        actual_api_url = api_url or os.getenv('API_URL') or "http://localhost:3000/api/tasks/generateConditional"
        print(f"🔗 API activat: {actual_api_url}")
    else:
        print("⚠️  API credentials nu sunt setate - nu voi apela API-ul")
    print("⏳ Astept PDF-uri noi... (Ctrl+C pentru a opri)\n")
    
    # Creeaza handler-ul si observer-ul
    event_handler = PDFHandler(api_key, max_pages, api_url, api_user, api_pass)
    observer = Observer()
    observer.schedule(event_handler, folder_path, recursive=False)
    observer.start()
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n\n🛑 Opresc monitorizarea...")
        observer.stop()
    
    observer.join()
    print("✅ Monitorizare oprita.")

def main():
    """Functia principala."""
    if len(sys.argv) < 2:
        print("Utilizare: python monitor_folder.py <folder_path> [api_key] [max_pages] [api_user] [api_pass] [api_url]")
        print("\nExemplu:")
        print("  python monitor_folder.py ~/Downloads")
        print("  python monitor_folder.py ~/Downloads sk-... 5")
        print("  python monitor_folder.py ~/Downloads sk-... 5 admin@example.com password123")
        print("  python monitor_folder.py ~/Downloads sk-... 5 admin@example.com pass https://api.example.com/api/tasks/generateConditional")
        print("\nSau seteaza variabile de mediu:")
        print("  export OPENAI_API_KEY='sk-...'")
        print("  export API_USER='admin@example.com'")
        print("  export API_PASS='password123'")
        print("  export API_URL='https://api.example.com/api/tasks/generateConditional'")
        sys.exit(1)
    
    folder_path = sys.argv[1]
    api_key = sys.argv[2] if len(sys.argv) > 2 and sys.argv[2].startswith('sk-') else None
    max_pages = 10
    api_user = os.getenv('API_USER')
    api_pass = os.getenv('API_PASS')
    api_url = os.getenv('API_URL')
    
    # Parse argumentele
    arg_index = 2
    if api_key:
        arg_index = 3
    
    # Urmatorul argument poate fi max_pages
    if len(sys.argv) > arg_index:
        try:
            max_pages = int(sys.argv[arg_index])
            arg_index += 1
        except ValueError:
            pass
    
    # Urmatoarele argumente pot fi api_user si api_pass
    if len(sys.argv) > arg_index:
        api_user = sys.argv[arg_index]
        arg_index += 1
    
    if len(sys.argv) > arg_index:
        api_pass = sys.argv[arg_index]
        arg_index += 1
    
    # Ultimul argument poate fi api_url
    if len(sys.argv) > arg_index:
        api_url = sys.argv[arg_index]
    
    monitor_folder(folder_path, api_key, max_pages, api_url, api_user, api_pass)

if __name__ == "__main__":
    main()
