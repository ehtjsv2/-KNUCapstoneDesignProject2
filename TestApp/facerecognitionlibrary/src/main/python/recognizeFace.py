import face_recognition
import PIL
from os.path import dirname, join
from PIL import Image
import numpy as np
import io
import matplotlib.pyplot as plt


def main(byteArr):
    try:
        # byteArray는 Android에서 전달받은 바이트 배열
        image = Image.open(io.BytesIO(byteArr))
        image = np.array(image)  # NumPy 배열로 변환
        print("Numpy 배열로 변환 완료")

        filename1 = join(dirname(__file__), "dohun001.jpg")
        filename2 = join(dirname(__file__), "dohun003.jpg")

        # print(filename1)
        # print(filename2)

        loaded_img1 = face_recognition.load_image_file(filename1)
        loaded_img2 = face_recognition.load_image_file(filename2)
        print("이미지 로드")

        print("128차원 특징 벡터 생성 중...(촬영된 이미지로부터)")
        # encoding_1 = face_recognition.face_encodings(loaded_img1)[0]
        encoding_1 = face_recognition.face_encodings(image)
        if len(encoding_1) > 0:
            encoding_1 = encoding_1[0]
        else:
            print("얼굴 인식 실패")
            return None

        encoding_2 = face_recognition.face_encodings(loaded_img2)[0]
        print("128차원 특징 벡터 생성 완료")

        # 두 얼굴 이미지의 특징 벡터 비교
        distance = face_recognition.face_distance([encoding_1], encoding_2)[0]
        print("유클리드 거리: ", distance)

        return distance

    except Exception as e:
        print("오류 발생:", str(e))
        return None