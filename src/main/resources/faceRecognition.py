import tensorflow as tf
import cv2
import numpy as np

from deepface import DeepFace
from deepface.commons import functions, distance as dst
from deepface.detectors import FaceDetector

import threading
from sys import argv
import ast

tf_version = tf.__version__
tf_major_version = int(tf_version.split(".", maxsplit=1)[0])

if tf_major_version == 1:
    from keras.preprocessing import image as image_lib
elif tf_major_version == 2:
    from tensorflow.keras.preprocessing import image as image_lib


# Connect camera
cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
if cap is None or not cap.isOpened():
    print('ERROR: unable to open video source')
    exit(1)


# Read input args
py_script, person_vector_str = argv
person_vector = ast.literal_eval(person_vector_str)


# Optimize GPU memory
gpus = tf.config.list_physical_devices('GPU')
if gpus:
    try:
        # Currently, memory growth needs to be the same across GPUs
        for gpu in gpus:
            tf.config.experimental.set_memory_growth(gpu, True)
        logical_gpus = tf.config.list_logical_devices('GPU')
    except RuntimeError as e:
        # Memory growth must be set before GPUs have been initialized
        print(e)


# Main variables
face_detector_name = "mediapipe"
face_detector = face_detection = FaceDetector.build_model(face_detector_name)

vector_model_name = "Facenet512"
vector_model = DeepFace.build_model(vector_model_name)

metric_met = 'cosine'
threshold = 0.4

frame_counter = 0


# Detect faces method
def detect_faces(face_detector_inner, img, align=True):
    resp = []

    img_width = img.shape[1]
    img_height = img.shape[0]

    results = face_detector_inner.process(img)

    if results.detections:
        for detection in results.detections:

            (confidence,) = detection.score

            bounding_box = detection.location_data.relative_bounding_box
            landmarks = detection.location_data.relative_keypoints

            x = int(bounding_box.xmin * img_width)
            w = int(bounding_box.width * img_width)
            y = int(bounding_box.ymin * img_height)
            h = int(bounding_box.height * img_height)

            left_eye = (int(landmarks[1].x * img_width), int(landmarks[1].y * img_height))
            right_eye = (int(landmarks[0].x * img_width), int(landmarks[0].y * img_height))

            if x > 0 and y > 0:
                detected_face = img[y: y + h, x: x + w]
                img_region = [x, y, w, h]

                if align:
                    detected_face = FaceDetector.alignment_procedure(detected_face, right_eye, left_eye)

                resp.append((detected_face, img_region, confidence))

    return resp


def reshape_image(image, target_size):
    if image.shape[0] > 0 and image.shape[1] > 0:
        face = []
        # resize and padding
        if image.shape[0] > 0 and image.shape[1] > 0:
            factor_0 = target_size[0] / image.shape[0]
            factor_1 = target_size[1] / image.shape[1]
            factor = min(factor_0, factor_1)

            dsize = (int(image.shape[1] * factor), int(image.shape[0] * factor))
            face = cv2.resize(image, dsize)

            diff_0 = target_size[0] - face.shape[0]
            diff_1 = target_size[1] - face.shape[1]

            # Put the base image in the middle of the padded image
            face = np.pad(
                face,
                (
                    (diff_0 // 2, diff_0 - diff_0 // 2),
                    (diff_1 // 2, diff_1 - diff_1 // 2),
                    (0, 0),
                ),
                "constant",
            )

        # double check: if target image is not still the same size with target.
        if face.shape[0:2] != target_size:
            face = cv2.resize(face, target_size)

        # normalizing the image pixels
        img_pixels = image_lib.img_to_array(face)  # what this line doing? must?
        img_pixels = np.expand_dims(img_pixels, axis=0)
        img_pixels /= 255  # normalize input in [0, 1]

        return img_pixels


def verify_face(face_to_verify):
    gpus_v = tf.config.list_physical_devices('GPU')
    if gpus:
        try:
            # Currently, memory growth needs to be the same across GPUs
            for gpu_v in gpus_v:
                tf.config.experimental.set_memory_growth(gpu_v, True)
        except RuntimeError as e:
            # Memory growth must be set before GPUs have been initialized
            print(e)
    target_size = functions.find_target_size(model_name=vector_model_name)
    reshaped_img = reshape_image(face_to_verify, target_size)
    vector_to_verify = vector_model.predict(reshaped_img, verbose=0)[0].tolist()
    print(vector_to_verify)
    distance = dst.findCosineDistance(vector_to_verify, person_vector)
    print(dst.findCosineDistance(vector_to_verify, person_vector))
    print(distance <= threshold)


# Main loop
while True:
    ret, frame = cap.read()

    if ret:
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        face_objs = detect_faces(face_detector, rgb_frame)
        if len(face_objs) != 0:
            for face_img, region, _ in face_objs:
                cv2.rectangle(frame, (region[0], region[1]), (region[0] + region[2], region[1] + region[3]),
                              (0, 0, 255), 1)
                if frame_counter % 30 == 0:
                    # verify_face(face_img.copy())
                    threading.Thread(target=verify_face, args=(face_img.copy(),)).start()

        cv2.imshow("camera", frame)

    frame_counter += 1

    key = cv2.waitKey(1)
    if key == ord("q"):
        break

cv2.destroyAllWindows()
