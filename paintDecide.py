import glob
from PIL import Image
import pyheif
import numpy as np
import datetime
import os

def imgGray(img): #モノクロ画像化
    return np.array(Image.open(img).convert('L'))

def makeHist(img):
    hist = [0 for i in range(256)]
    for i in range(len(img)):
        for j in range(len(img[0])):
            hist[img[i][j]]+=1;
    return hist

def histDist(a,b):
    s = 0
    for i in range(256):
        s+=abs(a[i]-b[i])
    return s

def hashNum(hashmap,num):
    if num in hashmap:
        hashmap[num]+=1
    else:
        hashmap.update({num:1})
    return hashmap[num]

def stradd(a):
    if len(str(a))==1:
        a = '0'+str(a)
    return a

size=6
temp = int(input('0:そのまま実行 1:データ更新してから実行 >>'))

avehist = [[0 for i in range(256)]for i in range(size)]

if temp==0:
    file = open("pict.txt",'r')
    li = file.readlines()
    for i in range(size):
        nums=li[i].split(' ')
        for j in range(256):
            avehist[i][j] = int(nums[j])
else:
    hist = [[[0 for i in range(256)]for i in range(5)]for k in range(size)]
    for i in range(size):
        print("{0}開始".format(i))
        for j in range(1,6):
            img = imgGray("testdata/{0}-{1}.JPG".format(i,j))
            hist[i][j-1] = makeHist(img)
            """for ii in range(img.shape(0)):
                for jj in range(img.shape(1)):
                    hist[i][j][img[ii][jj]]+=1;"""

        for j in range(256):
            n=0
            for k in range(5):
                n+=hist[i][k][j]
            avehist[i][j]=n//5
        print("{0}終了".format(i))
    file = open("pict.txt",'w')
    for i in range(size):
        for j in range(256):
            file.write(str(avehist[i][j]))
            if j != 255:
                file.write(' ')
        file.write("\n")
    file.close()

temp = int(input('最大値入力>>>'))
picts = glob.glob("data/*.HEIC")
temp = len(picts)
hashmap = [{} for i in range(4)]
for i in range(temp):
    if i%10==0:
        print("{0}開始".format(i))
    heic = pyheif.read(picts[i])
    dt = datetime.datetime.fromtimestamp(os.path.getmtime(picts[i]))
    date = "{0}-{1}-{2}".format(dt.year,stradd(dt.month),stradd(dt.day))
    data = Image.frombytes(heic.mode,heic.size,heic.data,"raw",heic.mode,heic.stride)
    img = np.array(data.convert('L'))
    histP = makeHist(img)
    di = histDist(histP,avehist[0])
    cla = 0
    for j in range(size):
        diL = histDist(histP,avehist[j])
        if(di>diL):
            di = diL
            cla = j
    path = ""
    number=1
    if cla == 0 or cla == 1:
        path = 'taiko/'
        number = hashNum(hashmap[0],date)
    elif cla == 2:
        path = 'sdvx/'
        number = hashNum(hashmap[1],date)
    elif cla == 3:
        path = 'gurukosu/'
        number = hashNum(hashmap[2],date)
    elif cla == 4 or cla == 5:
        path = 'tyuni/'
        number = hashNum(hashmap[3],date)
    data.save("{0}{1}-{2}.jpeg".format(path,date,number))
    os.utime("{0}{1}-{2}.jpeg".format(path,date,number),(dt.timestamp(), dt.timestamp()))
