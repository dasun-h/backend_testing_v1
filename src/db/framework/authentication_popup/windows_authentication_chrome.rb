require 'au3'

##################
# Main
##################
begin
  # Chrome
  if (ARGV.size != 1)
    puts("Usage: ruby windows_authentication_chrome.rb <screen width e.g. '1200'>")
    exit 1
  end

  # width
  width = ARGV[0].to_i
  
  if (width < 1 || 4000 < width )
    puts("Error: screen width #{width} is too small or too large. It should be between 1 and 4000.")
    exit 1
  end 

  # focus to the front chrome window
  if AutoItX3::Window.exists?("[CLASS:Chrome_WidgetWin_1]")
    win = AutoItX3::Window.new("[CLASS:Chrome_WidgetWin_1]") 
    win.activate
    sleep 1
    AutoItX3.mouse_click(width/2, 188)
    sleep 1
    AutoItX3.send_keys("username")
    AutoItX3.send_keys("{TAB}password")
    AutoItX3.send_keys("{TAB}{ENTER}")
    exit 0
  end
end
