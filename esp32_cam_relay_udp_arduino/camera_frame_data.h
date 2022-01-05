#include <esp_camera.h>
#define CAMERA_MODEL_AI_THINKER
#include "camera_pins.h"

void initCam(boolean);
int getFrame();

boolean cam_start_flg=false;
camera_fb_t *framecam;
uint8_t frame_flg='E';

static camera_config_t cam_conf =
{
  .pin_pwdn  = PWDN_GPIO_NUM,
  .pin_reset = RESET_GPIO_NUM,
  .pin_xclk = XCLK_GPIO_NUM,
  .pin_sscb_sda = SIOD_GPIO_NUM,
  .pin_sscb_scl = SIOC_GPIO_NUM,
  
  .pin_d7 = Y9_GPIO_NUM,
  .pin_d6 = Y8_GPIO_NUM,
  .pin_d5 = Y7_GPIO_NUM,
  .pin_d4 = Y6_GPIO_NUM,
  .pin_d3 = Y5_GPIO_NUM,
  .pin_d2 = Y4_GPIO_NUM,
  .pin_d1 = Y3_GPIO_NUM,
  .pin_d0 = Y2_GPIO_NUM,
  .pin_vsync = VSYNC_GPIO_NUM,
  .pin_href = HREF_GPIO_NUM,
  .pin_pclk = PCLK_GPIO_NUM,
  .xclk_freq_hz = 20000000,
  
  .ledc_timer = LEDC_TIMER_0,
  .ledc_channel = LEDC_CHANNEL_0,
  .pixel_format = PIXFORMAT_JPEG,
  .frame_size = FRAMESIZE_QVGA,
  .jpeg_quality = 12,
  .fb_count = 2
};

esp_err_t camera_init()
{
  if(PWDN_GPIO_NUM != -1)
  {
    pinMode(PWDN_GPIO_NUM, OUTPUT);
    digitalWrite(PWDN_GPIO_NUM, LOW);
  }
  esp_err_t err = esp_camera_init(&cam_conf);
  if(err != ESP_OK)
  {
    return err;
  }
  return ESP_OK;
}

esp_err_t camera_capture()
{
  framecam=esp_camera_fb_get();
  if(!framecam)
  {
    return ESP_FAIL;
  }
  esp_camera_fb_return(framecam);
  return ESP_OK;
}

void initCam(boolean a)
{
  if(a)
  {
    //initCameraConfiguration();
    if(camera_init()==ESP_OK)
    {
      //sensor_t *sensor_conf = esp_camera_sensor_get();
      //sensor_conf->set_hmirror(sensor_conf,1);
      cam_start_flg=true;
    }
    else
    {
      cam_start_flg=false;
    }
  }
  else
  {
    esp_camera_deinit();
  }
}

int getFrame()
{
  frame_flg='E';
  if(cam_start_flg)
  {
    if(camera_capture()==ESP_OK)
    {
      frame_flg='F';
    }
  }
  return (frame_flg=='F')?1:0;
}
