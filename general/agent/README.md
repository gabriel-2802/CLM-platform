# Agent pentru verificare achizitii intracomunitare

## Setup

1. Instaleaza poppler (necesar pentru pdf2image):
```bash
# Pe macOS
brew install poppler

# Pe Ubuntu/Debian
sudo apt-get install poppler-utils

# Pe Windows
# Descarca de la: https://github.com/oschwartz10612/poppler-windows/releases/
```

2. Creeaza si activeaza un virtual environment:
```bash
cd agent
python3 -m venv venv
source venv/bin/activate  # Pe macOS/Linux
# sau
venv\Scripts\activate  # Pe Windows
```

2. Instaleaza dependintele:
```bash
pip install -r requirements.txt
```

## Configurare API Key

Metoda 1 - Variabila de mediu (recomandat):
```bash
export OPENAI_API_KEY='sk-your-api-key-here'
```

Metoda 2 - Parametru direct:
```bash
python prompt.py document.pdf sk-your-api-key-here
```

## Utilizare

### Analiza unui singur PDF

```bash
# Cu API key din variabila de mediu
python prompt.py factura.pdf

# Cu API key ca parametru
python prompt.py factura.pdf sk-...
```

### Monitorizare folder pentru PDF-uri noi

```bash
# Monitorizeaza un folder (implicit primele 10 pagini per PDF)
python monitor_folder.py ~/Downloads

# Specifica numarul maxim de pagini
python monitor_folder.py ~/Downloads 5

# Cu API credentials pentru a apela backend-ul automat
python monitor_folder.py ~/Downloads sk-... 5 admin@example.com password123

# Cu server host custom
python monitor_folder.py ~/Downloads sk-... 5 admin@example.com pass https://api.example.com/api/tasks/generateConditional

# Sau seteaza variabile de mediu
export OPENAI_API_KEY='sk-...'
export API_USER='admin@example.com'
export API_PASS='password123'
export API_URL='https://api.example.com/api/tasks/generateConditional'
python monitor_folder.py ~/Downloads
```

Scriptul de monitorizare:
- Asteapta PDF-uri noi in folderul specificat
- Le analizeaza automat cand apar
- Salveaza rezultatele in fisiere `*_rezultat.txt` langa PDF-ul original
- Daca detecteaza achizitii intracomunitare:
  - Extrage `clientId`, `year`, `month` din calea fisierului (format: `<root>/<clientId>/<year>/<month>/file.pdf`)
  - Apeleaza automat API-ul `/api/tasks/generateConditional` pentru a crea/actualiza task-uri
  - Salveaza rezultatul API in `*_api_rezultat.json`
- Ruleaza continuu pana la Ctrl+C

## Functionalitate

Scriptul:
1. Primeste calea catre un fisier PDF
2. Il incarca si il trimite la ChatGPT API (GPT-4o)
3. Intreaba daca exista dovezi ale achizitiilor intracomunitare
4. Returneaza un raspuns detaliat cu:
   - Daca sunt achizitii intracomunitare
   - Gradul de certitudine
   - Dovezile gasite
   - Detalii explicative

## Ce cauta scriptul

- Mentiuni despre "achizitii intracomunitare"
- Facturi de la furnizori din alte state UE
- Coduri TVA din alte tari UE (ex: DE123456789, FR12345678901)
- Operatiuni intracomunitare
- Transport/livrari internationale in UE
- Documente vamale sau declaratii INTRASTAT
