# PowerChart
## 这是一个五边形的能力图表控件，以图标的形式形象的展示个属性的相对值。
## 使用方法：
### clone 到本地 以 library的形式添加依赖。
### layout.xml
    <cn.scrovor.powerchart.PowerView.PowerView
        android:layout_width="match_parent"
        android:layout_height="80dp"
        app:coreLength="30dp"            
        app:coverColor="#008800"   
        app:descTextSize="10dp"
        app:descTexts="@array/power_desc"
        app:descTextColor="@color/black"
        app:descNumColor="@color/grey"
        app:descNumberSize="6dp"
        app:curNumColor="@color/blue"/>
