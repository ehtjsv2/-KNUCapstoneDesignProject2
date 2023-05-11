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
    print("Numpy 배열로 변환 완료")

    print("128차원 특징 벡터 생성 중...")
    encoding_1 = face_recognition.face_encodings(image)[0]
    print("128차원 특징 벡터 생성 완료")

    return encoding_1
