import face_recognition
from os.path import dirname, join


def main():
    filename1 = join(dirname(__file__), "face1.jpg")
    filename2 = join(dirname(__file__), "face2.jpg")

    print(filename1)
    print(filename2)

    loaded_img1 = face_recognition.load_image_file(filename1)
    loaded_img2 = face_recognition.load_image_file(filename2)
    print("이미지 로드")

    encoding_1 = face_recognition.face_encodings(loaded_img1)[0]
    encoding_2 = face_recognition.face_encodings(loaded_img2)[0]
    print("128차원 특징 벡터 생성")

    # 두 얼굴 이미지의 특징 벡터 비교
    distance = face_recognition.face_distance([encoding_1], encoding_2)[0]
    print("유클리드 거리: ", distance)
