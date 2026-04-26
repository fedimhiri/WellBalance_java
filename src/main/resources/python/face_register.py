import cv2
import face_recognition
import sys
import os
import numpy as np

def register_face(username):
    # Dossier de stockage des visages
    save_dir = "faces"
    os.makedirs(save_dir, exist_ok=True)

    cap = cv2.VideoCapture(0)
    print(f"Enregistrement du visage pour : {username}")
    print("Appuyez sur ESPACE pour capturer, Q pour quitter")

    while True:
        ret, frame = cap.read()
        if not ret:
            print("ERROR: Impossible d'accéder à la caméra")
            break

        # Détecte les visages en temps réel
        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        locations = face_recognition.face_locations(rgb)

        for (top, right, bottom, left) in locations:
            cv2.rectangle(frame, (left, top), (right, bottom), (0, 255, 0), 2)
            cv2.putText(frame, "Visage détecté", (left, top - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 0), 2)

        cv2.imshow("Enregistrement - Appuyez ESPACE pour capturer", frame)

        key = cv2.waitKey(1) & 0xFF

        if key == ord(' '):  # ESPACE → capture
            if len(locations) == 0:
                print("ERROR: Aucun visage détecté")
                continue
            if len(locations) > 1:
                print("ERROR: Plusieurs visages détectés")
                continue

            # Encode et sauvegarde
            encodings = face_recognition.face_encodings(rgb, locations)
            if encodings:
                path = os.path.join(save_dir, f"{username}.npy")
                np.save(path, encodings[0])
                print(f"SUCCESS: Visage enregistré → {path}")
                break

        elif key == ord('q'):
            print("ERROR: Annulé par l'utilisateur")
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("ERROR: Username manquant")
        sys.exit(1)
    register_face(sys.argv[1])