import cv2
import face_recognition
import sys
import os
import numpy as np

def verify_face(username):
    face_path = os.path.join("faces", f"{username}.npy")

    if not os.path.exists(face_path):
        print("ERROR: Aucun visage enregistré pour cet utilisateur")
        return

    # Charge l'encodage enregistré
    known_encoding = np.load(face_path)

    cap = cv2.VideoCapture(0)
    print(f"Vérification du visage pour : {username}")
    print("Regardez la caméra...")

    attempts = 0
    max_attempts = 100  # ~10 secondes à 10fps

    while attempts < max_attempts:
        ret, frame = cap.read()
        if not ret:
            print("ERROR: Impossible d'accéder à la caméra")
            break

        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        locations = face_recognition.face_locations(rgb)
        encodings = face_recognition.face_encodings(rgb, locations)

        for (top, right, bottom, left), encoding in zip(locations, encodings):
            # Compare avec le visage enregistré
            results = face_recognition.compare_faces(
                [known_encoding], encoding, tolerance=0.5
            )
            distance = face_recognition.face_distance([known_encoding], encoding)[0]

            if results[0]:
                color = (0, 255, 0)
                label = f"Reconnu ({1 - distance:.0%})"
                cv2.rectangle(frame, (left, top), (right, bottom), color, 2)
                cv2.putText(frame, label, (left, top - 10),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)
                cv2.imshow("Verification", frame)
                cv2.waitKey(500)
                cap.release()
                cv2.destroyAllWindows()
                print("SUCCESS")
                return
            else:
                color = (0, 0, 255)
                label = f"Non reconnu ({1 - distance:.0%})"
                cv2.rectangle(frame, (left, top), (right, bottom), color, 2)
                cv2.putText(frame, label, (left, top - 10),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

        cv2.imshow("Verification - Regardez la caméra", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

        attempts += 1

    cap.release()
    cv2.destroyAllWindows()
    print("ERROR: Visage non reconnu")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("ERROR: Username manquant")
        sys.exit(1)
    verify_face(sys.argv[1])