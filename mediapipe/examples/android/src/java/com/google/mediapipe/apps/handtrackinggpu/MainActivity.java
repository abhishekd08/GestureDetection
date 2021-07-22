// Copyright 2019 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.mediapipe.apps.handtrackinggpu;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;

import com.google.mediapipe.components.GlSurfaceViewRenderer;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Main activity of MediaPipe hand tracking app. */
public class MainActivity extends com.google.mediapipe.apps.basic.MainActivity {
  private static final String TAG = "MainActivity";

  private static final String INPUT_NUM_HANDS_SIDE_PACKET_NAME = "num_hands";
  private static final String OUTPUT_LANDMARKS_STREAM_NAME = "hand_landmarks";
  // Max number of hands to detect/process.
  private static final int NUM_HANDS = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    AndroidPacketCreator packetCreator = processor.getPacketCreator();
    Map<String, Packet> inputSidePackets = new HashMap<>();
    inputSidePackets.put(INPUT_NUM_HANDS_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_HANDS));
    processor.setInputSidePackets(inputSidePackets);

    // To show verbose logging, run:
    // adb shell setprop log.tag.MainActivity VERBOSE
    if (Log.isLoggable(TAG, Log.VERBOSE)) {
      processor.addPacketCallback(
          OUTPUT_LANDMARKS_STREAM_NAME,
          (packet) -> {
            //Log.v(TAG, "Received multi-hand landmarks packet.");
            List<NormalizedLandmarkList> multiHandLandmarks =
                PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());
            /*Log.v(
                "", getMultiHandLandmarksDebugString(multiHandLandmarks));*/
            getMultiHandLandmarksDebugString(multiHandLandmarks);
          });
    }
  }

  private float minDist = 10;
  private float maxDist = 0;

  private String getMultiHandLandmarksDebugString(List<NormalizedLandmarkList> multiHandLandmarks) {
    if (multiHandLandmarks.isEmpty()) {
      return "No hand landmarks";
    }
    String multiHandLandmarksStr = "";
    multiHandLandmarksStr += "\n";

    NormalizedLandmarkList landmarks = multiHandLandmarks.get(0);
    NormalizedLandmark wrist = landmarks.getLandmark(0);
    /*NormalizedLandmark thumb_tip = landmarks.getLandmark(4);
    NormalizedLandmark index_tip = landmarks.getLandmark(8);*/
    NormalizedLandmark middle_tip = landmarks.getLandmark(12);
    /*NormalizedLandmark ring_tip = landmarks.getLandmark(16);
    NormalizedLandmark pinky_tip = landmarks.getLandmark(20);
    NormalizedLandmark pinky_mcp = landmarks.getLandmark(17);*/

    //float wrist_index = (float) Math.sqrt(Math.pow(wrist.getX() - index_tip.getX(),2) + Math.pow(wrist.getY() - index_tip.getY(),2));
    float wrist_middle = (float) Math.sqrt(Math.pow(wrist.getX() - middle_tip.getX(),2) + Math.pow(wrist.getY() - middle_tip.getY(),2));
    /*float wrist_ring = (float) Math.sqrt(Math.pow(wrist.getX() - ring_tip.getX(),2) + Math.pow(wrist.getY() - ring_tip.getY(),2));
    float wrist_pinky = (float) Math.sqrt(Math.pow(wrist.getX() - pinky_tip.getX(),2) + Math.pow(wrist.getY() - pinky_tip.getY(),2));
    float pinky_mcp_thumb_tip = (float) Math.sqrt(Math.pow(thumb_tip.getX() - pinky_mcp.getX(),2) + Math.pow(thumb_tip.getY() - pinky_mcp.getY(),2));*/

    float progress = 0;
    HandPoseEnum pose = getHandPose(multiHandLandmarks);
    if (pose == HandPoseEnum.PAPER && maxDist == 0) {
      maxDist = wrist_middle;
    }
    if (pose == HandPoseEnum.FIST){
      if (minDist > 9){
        minDist = wrist_middle;
        maxDist = maxDist - minDist;
      }
    }
    if ( maxDist > 0 && minDist < 9) {
      progress = ((wrist_middle - minDist)/maxDist) * 100;
      Log.v("", "" + progress);
    }

    return multiHandLandmarksStr;
  }

  HandPoseEnum getHandPose(List<NormalizedLandmarkList> multiHandLandmarks){
    NormalizedLandmarkList landmarks = multiHandLandmarks.get(0);
    NormalizedLandmark wrist = landmarks.getLandmark(0);
    NormalizedLandmark thumb_tip = landmarks.getLandmark(4);
    NormalizedLandmark index_mcp = landmarks.getLandmark(5);
    NormalizedLandmark index_tip = landmarks.getLandmark(8);
    NormalizedLandmark middle_mcp = landmarks.getLandmark(9);
    NormalizedLandmark middle_pip = landmarks.getLandmark(10);
    NormalizedLandmark middle_tip = landmarks.getLandmark(12);
    NormalizedLandmark ring_mcp = landmarks.getLandmark(13);
    NormalizedLandmark ring_tip = landmarks.getLandmark(16);
    NormalizedLandmark pinky_mcp = landmarks.getLandmark(17);
    NormalizedLandmark pinky_tip = landmarks.getLandmark(20);

    float thumb_tip_middle_pip = (float) Math.sqrt(Math.pow(thumb_tip.getX() - middle_pip.getX(),2) + Math.pow(thumb_tip.getY() - middle_pip.getY(),2));
    float thumb_tip_middle_mcp = (float) Math.sqrt(Math.pow(thumb_tip.getX() - middle_mcp.getX(),2) + Math.pow(thumb_tip.getY() - middle_mcp.getY(),2));
    float thumb_tip_index_mcp = (float) Math.sqrt(Math.pow(thumb_tip.getX() - index_mcp.getX(),2) + Math.pow(thumb_tip.getY() - index_mcp.getY(),2));

    float wrist_index_mcp = (float) Math.sqrt(Math.pow(index_mcp.getX() - wrist.getX(),2) + Math.pow(index_mcp.getY() - wrist.getY(),2));
    float wrist_middle_mcp = (float) Math.sqrt(Math.pow(middle_mcp.getX() - wrist.getX(),2) + Math.pow(middle_mcp.getY() - wrist.getY(),2));
    float wrist_ring_mcp = (float) Math.sqrt(Math.pow(ring_mcp.getX() - wrist.getX(),2) + Math.pow(ring_mcp.getY() - wrist.getY(),2));
    float wrist_pinky_mcp = (float) Math.sqrt(Math.pow(pinky_mcp.getX() - wrist.getX(),2) + Math.pow(pinky_mcp.getY() - wrist.getY(),2));

    float wrist_index_tip = (float) Math.sqrt(Math.pow(index_tip.getX() - wrist.getX(),2) + Math.pow(index_tip.getY() - wrist.getY(),2));
    float wrist_middle_tip = (float) Math.sqrt(Math.pow(middle_tip.getX() - wrist.getX(),2) + Math.pow(middle_tip.getY() - wrist.getY(),2));
    float wrist_ring_tip = (float) Math.sqrt(Math.pow(ring_tip.getX() - wrist.getX(),2) + Math.pow(ring_tip.getY() - wrist.getY(),2));
    float wrist_pinky_tip = (float) Math.sqrt(Math.pow(pinky_tip.getX() - wrist.getX(),2) + Math.pow(pinky_tip.getY() - wrist.getY(),2));

    boolean isThumbOpen = isThumbOpen(wrist_middle_mcp, wrist_middle_tip, thumb_tip_middle_pip,
            thumb_tip_middle_mcp, thumb_tip_index_mcp);
    boolean isIndexOpen = isIndexOpen(wrist_index_mcp, wrist_index_tip);
    boolean isMiddleOpen = isMiddleOpen(wrist_middle_mcp, wrist_middle_tip);
    boolean isRingOpen = isRingOpen(wrist_ring_mcp, wrist_ring_tip);
    boolean isPinkyOpen = isPinkyOpen(wrist_pinky_mcp, wrist_pinky_tip);

    HandPoseEnum handPose;
    if (!isThumbOpen && !isIndexOpen && !isMiddleOpen && !isRingOpen && !isPinkyOpen){
      handPose = HandPoseEnum.FIST;
    } else if (isThumbOpen && isIndexOpen && isMiddleOpen && isRingOpen && isPinkyOpen) {
      handPose = HandPoseEnum.PAPER;
    } else if (!isThumbOpen && isIndexOpen && !isMiddleOpen && !isRingOpen && !isPinkyOpen) {
      handPose = HandPoseEnum.ONE;
    } else if (!isThumbOpen && isIndexOpen && isMiddleOpen && !isRingOpen && !isPinkyOpen) {
      handPose = HandPoseEnum.TWO;
    } else if (!isThumbOpen && isIndexOpen && isMiddleOpen && isRingOpen && !isPinkyOpen) {
      handPose = HandPoseEnum.THREE;
    } else if (!isThumbOpen && isIndexOpen && isMiddleOpen && isRingOpen && isPinkyOpen) {
      handPose = HandPoseEnum.FOUR;
    } else if (isThumbOpen && !isIndexOpen && !isMiddleOpen && !isRingOpen && !isPinkyOpen) {
      handPose = HandPoseEnum.THUMB;
    } else if (isThumbOpen && isIndexOpen && !isMiddleOpen && !isRingOpen && !isPinkyOpen) {
      handPose = HandPoseEnum.RIGHT;
    } else if (!isThumbOpen && isIndexOpen && isMiddleOpen && !isRingOpen && !isPinkyOpen) {
      handPose = HandPoseEnum.PEACE;
    } else if (!isThumbOpen && isIndexOpen && !isMiddleOpen && !isRingOpen && isPinkyOpen) {
      handPose = HandPoseEnum.SPIDERMAN;
    } else {
      handPose = HandPoseEnum.UNKNOWN;
    }

    return handPose;
  }

  private boolean isIndexOpen(float wrist_index_mcp, float wrist_index_tip){
    return wrist_index_mcp > wrist_index_tip ? false : true;
  }

  private boolean isMiddleOpen(float wrist_middle_mcp, float wrist_middle_tip){
    return wrist_middle_mcp > wrist_middle_tip ? false : true;
  }

  private boolean isRingOpen(float wrist_ring_mcp, float wrist_ring_tip){
    return wrist_ring_mcp > wrist_ring_tip ? false : true;
  }

  private boolean isPinkyOpen(float wrist_pinky_mcp, float wrist_pinky_tip){
    return wrist_pinky_mcp > wrist_pinky_tip ? false : true;
  }

  private boolean isThumbOpen(float wrist_middle_mcp, float wrist_middle_tip,
                              float thumb_tip_middle_pip, float thumb_tip_middle_mcp,
                              float thumb_tip_index_mcp){
    boolean isThumbOpen = false;
    if (wrist_middle_mcp > wrist_middle_tip) {
      if (thumb_tip_middle_pip < 0.08) {
      } else {
        isThumbOpen = true;
      }
    } else {
      if (thumb_tip_middle_mcp < 0.08 || thumb_tip_index_mcp < 0.08) {
      } else {
        isThumbOpen = true;
      }
    }
    return isThumbOpen;
  }

  /*private boolean isIndexOpen(NormalizedLandmarkList landmarks){
    NormalizedLandmark wrist = landmarks.getLandmark(0);
    NormalizedLandmark index_mcp = landmarks.getLandmark(5);
    NormalizedLandmark index_tip = landmarks.getLandmark(8);

    float wrist_index_mcp = (float) Math.sqrt(Math.pow(index_mcp.getX() - wrist.getX(),2) + Math.pow(index_mcp.getY() - wrist.getY(),2));
    float wrist_index_tip = (float) Math.sqrt(Math.pow(index_tip.getX() - wrist.getX(),2) + Math.pow(index_tip.getY() - wrist.getY(),2));

    return wrist_index_mcp > wrist_index_tip ? false : true;
  }

  private boolean isMiddleOpen(NormalizedLandmarkList landmarks){
    NormalizedLandmark wrist = landmarks.getLandmark(0);
    NormalizedLandmark middle_mcp = landmarks.getLandmark(9);
    NormalizedLandmark middle_tip = landmarks.getLandmark(12);

    float wrist_middle_mcp = (float) Math.sqrt(Math.pow(middle_mcp.getX() - wrist.getX(),2) + Math.pow(middle_mcp.getY() - wrist.getY(),2));
    float wrist_middle_tip = (float) Math.sqrt(Math.pow(middle_tip.getX() - wrist.getX(),2) + Math.pow(middle_tip.getY() - wrist.getY(),2));
    return wrist_middle_mcp > wrist_middle_tip ? false : true;
  }

  private boolean isRingOpen(NormalizedLandmarkList landmarks){
    NormalizedLandmark wrist = landmarks.getLandmark(0);
    NormalizedLandmark ring_mcp = landmarks.getLandmark(13);
    NormalizedLandmark ring_tip = landmarks.getLandmark(16);

    float wrist_ring_mcp = (float) Math.sqrt(Math.pow(ring_mcp.getX() - wrist.getX(),2) + Math.pow(ring_mcp.getY() - wrist.getY(),2));
    float wrist_ring_tip = (float) Math.sqrt(Math.pow(ring_tip.getX() - wrist.getX(),2) + Math.pow(ring_tip.getY() - wrist.getY(),2));

    return wrist_ring_mcp > wrist_ring_tip ? false : true;
  }

  private boolean isPinkyOpen(NormalizedLandmarkList landmarks){
    NormalizedLandmark wrist = landmarks.getLandmark(0);
    NormalizedLandmark pinky_mcp = landmarks.getLandmark(17);
    NormalizedLandmark pinky_tip = landmarks.getLandmark(20);

    float wrist_pinky_mcp = (float) Math.sqrt(Math.pow(pinky_mcp.getX() - wrist.getX(),2) + Math.pow(pinky_mcp.getY() - wrist.getY(),2));
    float wrist_pinky_tip = (float) Math.sqrt(Math.pow(pinky_tip.getX() - wrist.getX(),2) + Math.pow(pinky_tip.getY() - wrist.getY(),2));

    return wrist_pinky_mcp > wrist_pinky_tip ? false : true;
  }*/

}
