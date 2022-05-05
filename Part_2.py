#!/usr/bin/env python
# coding: utf-8

# In[5]:
import sys

import cv2
import numpy as np
from PIL import Image
from matplotlib import pyplot as plt

print('argument list:', str(sys.argv))
with open("dataset\Videos\data_test1.rgb", "rb") as f:
   v = np.fromfile(f, np.dtype('B'))



#setting the number of pixels in frame
pxct = 129600
#setting the number of frames
frct = (int)(v.size/(3*pxct))



final_video = []
for frame_number in range(frct):
        start = (frame_number) * pxct * 3
        curr_frame = v[start:start + 3 * pxct]
        final_frame = np.reshape(curr_frame, ( 480,270, 3), order = 'F')
        final_frame = np.rot90(final_frame,3)
        final_frame = np.flip(final_frame,1)
        final_video.append(final_frame)
#print(final_video)

out = cv2.VideoWriter('video.avi', cv2.VideoWriter_fourcc(*'DIVX'), 30, (480, 270))

for i in range(frct):
    out.write(final_video[i])

out.release()



from skimage.metrics import structural_similarity



# Convert images to grayscale
i1 = cv2.cvtColor(final_video[100], cv2.COLOR_BGR2GRAY)
i2 = cv2.cvtColor(final_video[2000], cv2.COLOR_BGR2GRAY)



(score, diff) = structural_similarity(i1, i2, full=True)
diff = (diff * 255).astype("uint8")
print("Structural Similarity Index: {}".format(score))


