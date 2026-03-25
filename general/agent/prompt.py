#!/usr/bin/env python3
"""
Script pentru verificarea achizitiilor intracomunitare din PDF-uri folosind ChatGPT API.
"""

import sys
import os
from pathlib import Path
from openai import OpenAI
import base64
from pdf2image import convert_from_path
from io import BytesIO

def convert_pdf_to_images_base64(pdf_path: str, max_pages: int = 10) -> list:
    """
    Converteste paginile PDF-ului in imagini si le returneaza ca base64.
    
    Args:
        pdf_path: Calea catre fisierul PDF
        max_pages: Numarul maxim de pagini de procesat
    
    Returns:
        list: Lista cu imagini in format base64
    """
    try:
        # Converteste PDF in imagini (prima pagina sau primele max_pages pagini)
        images = convert_from_path(pdf_path, dpi=200, first_page=1, last_page=max_pages)
        
        base64_images = []
        for img in images:
            # Converteste imaginea in bytes
            buffer = BytesIO()
            img.save(buffer, format='PNG')
            buffer.seek(0)
            
            # Encode ca base64
            img_base64 = base64.b64encode(buffer.read()).decode('utf-8')
            base64_images.append(img_base64)
        
        return base64_images
    except Exception as e:
        raise Exception(f"Eroare la conversia PDF in imagini: {str(e)}")

def check_achizitii_intracomunitare(pdf_path: str, api_key: str = None, max_pages: int = 10) -> dict:
    """
    Verifica daca un PDF contine dovezi ale achizitiilor intracomunitare.
    
    Args:
        pdf_path: Calea catre fisierul PDF
        api_key: OpenAI API key (optional, poate fi setat ca variabila de mediu)
    
    Returns:
        dict: Raspunsul ChatGPT cu analiza documentului
    """
    if not os.path.exists(pdf_path):
        return {"error": f"Fisierul {pdf_path} nu exista"}
    
    if not pdf_path.lower().endswith('.pdf'):
        return {"error": "Fisierul trebuie sa fie un PDF"}
    
    # Initializeaza clientul OpenAI
    if api_key:
        client = OpenAI(api_key=api_key)
    else:
        # Va incerca sa foloseasca OPENAI_API_KEY din variabilele de mediu
        client = OpenAI()
    
    try:
        # Converteste PDF-ul in imagini
        print(f"Convertesc PDF-ul in imagini (max {max_pages} pagini)...")
        images_base64 = convert_pdf_to_images_base64(pdf_path, max_pages)
        print(f"Am convertit {len(images_base64)} pagini in imagini")
        
        # Prompt pentru ChatGPT
        prompt = """
        Analizeaza acest document si determina daca exista dovezi ale achizitiilor intracomunitare.
        
        Achizitiile intracomunitare sunt tranzactii comerciale intre companii din state membre UE diferite.
        
        Cauta urmatoarele indicii:
        - Mentiuni despre "achizitii intracomunitare"
        - Facturi sau documente de la furnizori din alte state membre UE
        - Coduri TVA din alte tari UE (format: prefix tara + cifre, ex: DE123456789, FR12345678901)
        - Mentiuni despre "operatiuni intracomunitare"
        - Transport/livrari internationale in UE
        - Documente vamale sau declaratii INTRASTAT
        
        Raspunde in format JSON cu urmatoarele campuri:
        - are_achizitii_intracomunitare: true/false
        - grad_certitudine: "sigur" / "probabil" / "posibil" / "improbabil"
        - dovezi_gasite: lista cu dovezile identificate
        - detalii: explicatie pe scurt
        """
        
        # Construieste content-ul pentru API
        content = [{"type": "text", "text": prompt}]
        
        # Adauga toate imaginile
        for img_base64 in images_base64:
            content.append({
                "type": "image_url",
                "image_url": {
                    "url": f"data:image/png;base64,{img_base64}"
                }
            })
        
        # Apel API ChatGPT cu GPT-4 Vision
        print("Trimit imaginile catre ChatGPT API...")
        response = client.chat.completions.create(
            model="gpt-4o",
            messages=[
                {
                    "role": "user",
                    "content": content
                }
            ],
            max_tokens=1500,
            temperature=0.3
        )
        
        # Extrage raspunsul
        answer = response.choices[0].message.content
        
        return {
            "success": True,
            "pdf_path": pdf_path,
            "raspuns": answer,
            "tokens_used": response.usage.total_tokens,
            "pages_processed": len(images_base64)
        }
        
    except Exception as e:
        return {
            "success": False,
            "error": str(e),
            "pdf_path": pdf_path
        }

def main():
    """Functia principala."""
    if len(sys.argv) < 2:
        print("Utilizare: python check_achizitii_intracomunitare.py <cale_pdf> [api_key]")
        print("\nExemplu:")
        print("  python check_achizitii_intracomunitare.py factura.pdf")
        print("  python check_achizitii_intracomunitare.py factura.pdf sk-...")
        print("\nSau seteaza OPENAI_API_KEY ca variabila de mediu:")
        print("  export OPENAI_API_KEY='sk-...'")
        sys.exit(1)
    
    pdf_path = sys.argv[1]
    api_key = sys.argv[2] if len(sys.argv) > 2 else None
    
    print(f"Analizez fisierul: {pdf_path}")
    print("Se apeleaza ChatGPT API...")
    
    result = check_achizitii_intracomunitare(pdf_path, api_key)
    
    if result.get("success"):
        print("\n" + "="*60)
        print("REZULTAT ANALIZA")
        print("="*60)
        print(result["raspuns"])
        print("\n" + "="*60)
        print(f"Tokens folositi: {result.get('tokens_used', 'N/A')}")
    else:
        print(f"\nEroare: {result.get('error')}")
        sys.exit(1)

if __name__ == "__main__":
    main()
