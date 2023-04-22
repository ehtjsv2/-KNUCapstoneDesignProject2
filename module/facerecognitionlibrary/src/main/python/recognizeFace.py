import face_recognition
import PIL
from os.path import dirname, join
from PIL import Image
import numpy as np
import io
import matplotlib.pyplot as plt


def main(byteArr):

    # byteArray는 Android에서 전달받은 바이트 배열
    image = Image.open(io.BytesIO(byteArr))
    image = np.array(image)  # NumPy 배열로 변환

    filename1 = join(dirname(__file__), "face1.jpg")
    filename2 = join(dirname(__file__), "dohun003.jpg")

    # print(filename1)
    # print(filename2)

    loaded_img1 = face_recognition.load_image_file(filename1)
    loaded_img2 = face_recognition.load_image_file(filename2)
    print("이미지 로드")

    # encoding_1 = face_recognition.face_encodings(loaded_img1)[0]
    encoding_1 = face_recognition.face_encodings(image)[0]
    encoding_2 = face_recognition.face_encodings(loaded_img2)[0]
    print("128차원 특징 벡터 생성")

    # 두 얼굴 이미지의 특징 벡터 비교
    distance = face_recognition.face_distance([encoding_1], encoding_2)[0]
    print("유클리드 거리: ", distance)
