from flask import Flask, jsonify, request
from flask_cors import CORS
import requests

app = Flask(__name__)
CORS(app)

# Bağlantı Ayarları
SUPABASE_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im9uYXZxdnl6YnJoeXdvbGJha3FpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQyNDM4NDksImV4cCI6MjA3OTgxOTg0OX0.NV7DqgdsYex1M3b_untQTpTgmNWs1LCUEJ_hcksVUXg'

def get_headers():
    return {
        'apikey': SUPABASE_KEY,
        'Authorization': f'Bearer {SUPABASE_KEY}',
        'Content-Type': 'application/json'
    }

@app.route('/')
def index():
    return '''<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <title>Toplantı Katılım Süreleri</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; background: #f4f7f6; padding: 20px; }
        .container { background: white; padding: 30px; border-radius: 12px; max-width: 1200px; margin: auto; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        .filter-section { display: flex; gap: 15px; align-items: center; margin-bottom: 25px; background: #eee; padding: 15px; border-radius: 8px; flex-wrap: wrap; }
        select { padding: 10px; border-radius: 5px; border: 1px solid #ccc; min-width: 300px; }
        .user-count { background: #4a90e2; color: white; padding: 10px 20px; border-radius: 5px; font-weight: bold; }
        .end-session-btn { background: #dc3545; color: white; padding: 10px 20px; border: none; border-radius: 5px; font-weight: bold; cursor: pointer; transition: 0.3s; }
        .end-session-btn:hover { background: #c82333; }
        .end-session-btn:active { transform: scale(0.98); }
        
        .table-section { margin-bottom: 40px; }
        .table-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
        .table-title { font-size: 1.3em; font-weight: bold; color: #333; }
        .export-btn { padding: 8px 16px; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer; transition: 0.3s; }
        .export-btn:hover { background: #218838; }
        
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { border-bottom: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background: #4a90e2; color: white; }
        .badge-time { padding: 4px 10px; border-radius: 20px; font-weight: bold; background: #e3f2fd; color: #1976d2; }
        
        .pagination-controls { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 10px; border-top: 1px solid #eee; }
        .btn-page { padding: 8px 16px; cursor: pointer; background: #4a90e2; color: white; border: none; border-radius: 4px; transition: 0.3s; }
        .btn-page:disabled { background: #ccc; cursor: not-allowed; }
        .page-indicator { font-weight: bold; color: #555; }
        
        .loading { text-align: center; color: #666; font-style: italic; }
        .error { text-align: center; color: red; font-weight: bold; }
    </style>
    
    <!-- SheetJS library for Excel export -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
</head>
<body>
    <div class="container">
        <h1>⏱️ Toplantı Katılım Süreleri</h1>
        
        <div class="filter-section">
            <label><strong>Toplantı Seçin:</strong></label>
            <select id="meetingSelect" onchange="fetchNewData()">
                <option value="">Yükleniyor...</option>
            </select>
            <div class="user-count" id="userCount" style="display:none;">
                <span id="userCountText">Toplantıdaki Kişi: 0</span>
            </div>
            <button class="end-session-btn" id="endSessionBtn" onclick="endActiveSessions()" style="display:none;">
                🔴 Oturumu Sonlandır
            </button>
        </div>

        <!-- Tablo 1: Toplantı Süreleri -->
        <div class="table-section">
            <div class="table-header">
                <h2 class="table-title">📊 Toplantı Süreleri</h2>
                <button class="export-btn" onclick="exportToExcel('table1')" id="exportBtn1" style="display:none;">Excel'e Aktar</button>
            </div>
            <table id="dataTable1">
                <thead>
                    <tr>
                        <th>Katılımcı Adı Soyadı</th>
                        <th>Toplam Süre (Dakika)</th>
                    </tr>
                </thead>
                <tbody id="tableBody1">
                    <tr><td colspan="2" class="loading">Lütfen listeden bir toplantı seçin.</td></tr>
                </tbody>
            </table>
            <div class="pagination-controls" id="paginationUI1" style="display:none;">
                <button class="btn-page" id="prevBtn1" onclick="changePage('table1', -1)">Geri</button>
                <span class="page-indicator" id="pageIndicator1">Sayfa 1 / 1</span>
                <button class="btn-page" id="nextBtn1" onclick="changePage('table1', 1)">İleri</button>
            </div>
        </div>

        <!-- Tablo 2: Toplantıya Katılmayanlar -->
        <div class="table-section">
            <div class="table-header">
                <h2 class="table-title">❌ Toplantıya Katılmayanlar</h2>
                <button class="export-btn" onclick="exportToExcel('table2')" id="exportBtn2" style="display:none;">Excel'e Aktar</button>
            </div>
            <table id="dataTable2">
                <thead>
                    <tr>
                        <th>Katılımcı Adı Soyadı</th>
                    </tr>
                </thead>
                <tbody id="tableBody2">
                    <tr><td class="loading">Lütfen listeden bir toplantı seçin.</td></tr>
                </tbody>
            </table>
            <div class="pagination-controls" id="paginationUI2" style="display:none;">
                <button class="btn-page" id="prevBtn2" onclick="changePage('table2', -1)">Geri</button>
                <span class="page-indicator" id="pageIndicator2">Sayfa 1 / 1</span>
                <button class="btn-page" id="nextBtn2" onclick="changePage('table2', 1)">İleri</button>
            </div>
        </div>

        <!-- Tablo 3: Toplantıdan Kaçanlar -->
        <div class="table-section">
            <div class="table-header">
                <div style="display: flex; align-items: center; gap: 15px;">
                    <h2 class="table-title">🚪 Toplantıdan Kaçanlar</h2>
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <label style="font-size: 0.9em; color: #555;">Son</label>
                        <select id="minuteSelect" onchange="reloadTable3()" style="padding: 6px 10px; border-radius: 4px; border: 1px solid #ccc; font-size: 0.95em;">
                            <option value="5" selected>5</option>
                            <option value="10">10</option>
                            <option value="15">15</option>
                            <option value="20">20</option>
                            <option value="30">30</option>
                        </select>
                        <label style="font-size: 0.9em; color: #555;">dk olmayanlar</label>
                    </div>
                </div>
                <button class="export-btn" onclick="exportToExcel('table3')" id="exportBtn3" style="display:none;">Excel'e Aktar</button>
            </div>
            <table id="dataTable3">
                <thead>
                    <tr>
                        <th>Katılımcı Adı Soyadı</th>
                    </tr>
                </thead>
                <tbody id="tableBody3">
                    <tr><td class="loading">Lütfen listeden bir toplantı seçin.</td></tr>
                </tbody>
            </table>
            <div class="pagination-controls" id="paginationUI3" style="display:none;">
                <button class="btn-page" id="prevBtn3" onclick="changePage('table3', -1)">Geri</button>
                <span class="page-indicator" id="pageIndicator3">Sayfa 1 / 1</span>
                <button class="btn-page" id="nextBtn3" onclick="changePage('table3', 1)">İleri</button>
            </div>
        </div>
    </div>

    <script>
        const tableData = {
            table1: { allData: [], currentPage: 1, rowsPerPage: 10 },
            table2: { allData: [], currentPage: 1, rowsPerPage: 10 },
            table3: { allData: [], currentPage: 1, rowsPerPage: 10 }
        };

        window.onload = async function() {
            try {
                const response = await fetch('/api/meetings');
                const meetings = await response.json();
                const select = document.getElementById('meetingSelect');
                select.innerHTML = '<option value="">-- Bir Toplantı Seçin --</option>';
                meetings.forEach(m => {
                    const opt = document.createElement('option');
                    opt.value = m.id;
                    opt.textContent = m.Name;
                    select.appendChild(opt);
                });
            } catch (err) { 
                alert("Toplantı listesi alınamadı!"); 
            }
        };

        async function fetchNewData() {
            const meetingId = document.getElementById('meetingSelect').value;
            if (!meetingId) {
                // Toplantı seçilmediğinde tüm tabloları sıfırla
                resetAllTables();
                return;
            }

            // Tüm tabloları sıfırla ve yükleme durumuna al
            resetAllTables();
            
            // Kişi sayısını getir
            await fetchUserCount(meetingId);
            
            // Tabloları sırayla yükle
            await loadTable1(meetingId);
            await loadTable2(meetingId);
            await loadTable3(meetingId);
        }

        async function fetchUserCount(meetingId) {
            try {
                const response = await fetch('/api/user-count', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ meeting_id: meetingId })
                });
                const result = await response.json();
                
                const userCountDiv = document.getElementById('userCount');
                const userCountText = document.getElementById('userCountText');
                const endSessionBtn = document.getElementById('endSessionBtn');
                
                if (result.count !== undefined) {
                    userCountText.textContent = `Toplantıdaki Kişi: ${result.count}`;
                    userCountDiv.style.display = 'block';
                    endSessionBtn.style.display = 'block';
                } else {
                    userCountDiv.style.display = 'none';
                    endSessionBtn.style.display = 'none';
                }
            } catch (err) {
                console.error('Kişi sayısı alınamadı:', err);
                document.getElementById('userCount').style.display = 'none';
                document.getElementById('endSessionBtn').style.display = 'none';
            }
        }

        async function loadTable1(meetingId) {
            const tbody = document.getElementById('tableBody1');
            tbody.innerHTML = '<tr><td colspan="2" class="loading">Veriler yükleniyor...</td></tr>';

            try {
                const response = await fetch('/api/data/table1', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ meeting_id: meetingId })
                });
                const result = await response.json();
                
                if (result.error) {
                    throw new Error(result.error);
                }
                
                tableData.table1.allData = result.data || [];
                tableData.table1.currentPage = 1;
                renderTable('table1');
                document.getElementById('exportBtn1').style.display = tableData.table1.allData.length > 0 ? 'block' : 'none';
            } catch (err) {
                tbody.innerHTML = '<tr><td colspan="2" class="error">Hata oluştu: ' + err.message + '</td></tr>';
                document.getElementById('exportBtn1').style.display = 'none';
            }
        }

        async function loadTable2(meetingId) {
            const tbody = document.getElementById('tableBody2');
            tbody.innerHTML = '<tr><td class="loading">Veriler yükleniyor...</td></tr>';

            try {
                const response = await fetch('/api/data/table2', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ meeting_id: meetingId })
                });
                const result = await response.json();
                
                if (result.error) {
                    throw new Error(result.error);
                }
                
                tableData.table2.allData = result.data || [];
                tableData.table2.currentPage = 1;
                renderTable('table2');
                document.getElementById('exportBtn2').style.display = tableData.table2.allData.length > 0 ? 'block' : 'none';
            } catch (err) {
                tbody.innerHTML = '<tr><td class="error">Hata oluştu: ' + err.message + '</td></tr>';
                document.getElementById('exportBtn2').style.display = 'none';
            }
        }

        async function loadTable3(meetingId) {
            const tbody = document.getElementById('tableBody3');
            tbody.innerHTML = '<tr><td class="loading">Veriler yükleniyor...</td></tr>';

            try {
                const minutes = document.getElementById('minuteSelect').value;
                
                const response = await fetch('/api/data/table3', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ 
                        meeting_id: meetingId,
                        minutes: parseInt(minutes)
                    })
                });
                const result = await response.json();
                
                if (result.error) {
                    throw new Error(result.error);
                }
                
                tableData.table3.allData = result.data || [];
                tableData.table3.currentPage = 1;
                renderTable('table3');
                document.getElementById('exportBtn3').style.display = tableData.table3.allData.length > 0 ? 'block' : 'none';
            } catch (err) {
                tbody.innerHTML = '<tr><td class="error">Hata oluştu: ' + err.message + '</td></tr>';
                document.getElementById('exportBtn3').style.display = 'none';
            }
        }

        function reloadTable3() {
            const meetingId = document.getElementById('meetingSelect').value;
            if (meetingId) {
                loadTable3(meetingId);
            }
        }

        async function endActiveSessions() {
            const meetingId = document.getElementById('meetingSelect').value;
            if (!meetingId) {
                alert('Lütfen bir toplantı seçin!');
                return;
            }

            if (!confirm('Tüm aktif oturumları sonlandırmak istediğinizden emin misiniz?')) {
                return;
            }

            try {
                const response = await fetch('/api/end-sessions', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ meeting_id: meetingId })
                });
                
                const result = await response.json();
                
                if (result.error) {
                    alert('Hata: ' + result.error);
                } else {
                    alert('Oturumlar başarıyla sonlandırıldı!');
                    // Verileri yenile
                    await fetchNewData();
                }
            } catch (err) {
                alert('Oturumlar sonlandırılırken hata oluştu: ' + err.message);
            }
        }

        function resetAllTables() {
            // Tablo 1
            document.getElementById('tableBody1').innerHTML = '<tr><td colspan="2" class="loading">Lütfen listeden bir toplantı seçin.</td></tr>';
            document.getElementById('paginationUI1').style.display = 'none';
            document.getElementById('exportBtn1').style.display = 'none';
            
            // Tablo 2
            document.getElementById('tableBody2').innerHTML = '<tr><td class="loading">Lütfen listeden bir toplantı seçin.</td></tr>';
            document.getElementById('paginationUI2').style.display = 'none';
            document.getElementById('exportBtn2').style.display = 'none';
            
            // Tablo 3
            document.getElementById('tableBody3').innerHTML = '<tr><td class="loading">Lütfen listeden bir toplantı seçin.</td></tr>';
            document.getElementById('paginationUI3').style.display = 'none';
            document.getElementById('exportBtn3').style.display = 'none';
            
            // Kişi sayısını ve oturum sonlandır butonunu gizle
            document.getElementById('userCount').style.display = 'none';
            document.getElementById('endSessionBtn').style.display = 'none';
        }

        function renderTable(tableId) {
            const data = tableData[tableId];
            const tbody = document.getElementById(`tableBody${tableId.slice(-1)}`);
            const paginationUI = document.getElementById(`paginationUI${tableId.slice(-1)}`);
            
            if (data.allData.length === 0) {
                const colSpan = tableId === 'table1' ? 2 : 1;
                tbody.innerHTML = `<tr><td colspan="${colSpan}" class="loading">Kayıt bulunamadı.</td></tr>`;
                paginationUI.style.display = 'none';
                return;
            }

            paginationUI.style.display = 'flex';
            
            // Pagination hesaplamaları
            const totalPages = Math.ceil(data.allData.length / data.rowsPerPage);
            const start = (data.currentPage - 1) * data.rowsPerPage;
            const end = start + data.rowsPerPage;
            const paginatedItems = data.allData.slice(start, end);

            // Tabloyu doldur
            if (tableId === 'table1') {
                tbody.innerHTML = paginatedItems.map(row => `
                    <tr>
                        <td><strong>${row.name || 'N/A'}</strong></td>
                        <td><span class="badge-time">${row.duration || 0} dk</span></td>
                    </tr>
                `).join('');
            } else {
                // Tablo 2 ve 3 için name + surname formatı
                tbody.innerHTML = paginatedItems.map(row => {
                    const fullName = (row.name || row.Name || '') + ' ' + (row.surname || row.Surname || '');
                    return `
                        <tr>
                            <td><strong>${fullName.trim() || 'N/A'}</strong></td>
                        </tr>
                    `;
                }).join('');
            }

            // Kontrolleri güncelle
            const pageNum = tableId.slice(-1);
            document.getElementById(`pageIndicator${pageNum}`).innerText = `Sayfa ${data.currentPage} / ${totalPages}`;
            document.getElementById(`prevBtn${pageNum}`).disabled = data.currentPage === 1;
            document.getElementById(`nextBtn${pageNum}`).disabled = data.currentPage === totalPages;
        }

        function changePage(tableId, direction) {
            tableData[tableId].currentPage += direction;
            renderTable(tableId);
        }

        function exportToExcel(tableId) {
            const data = tableData[tableId];
            
            if (data.allData.length === 0) {
                alert('Dışa aktarılacak veri bulunamadı!');
                return;
            }

            let worksheetData = [];
            let fileName = '';

            if (tableId === 'table1') {
                fileName = 'Toplanti_Sureleri.xlsx';
                worksheetData = [
                    ['Katılımcı Adı Soyadı', 'Toplam Süre (Dakika)'],
                    ...data.allData.map(row => [row.name || 'N/A', row.duration || 0])
                ];
            } else if (tableId === 'table2') {
                fileName = 'Toplantiya_Katilmayanlar.xlsx';
                worksheetData = [
                    ['Katılımcı Adı Soyadı'],
                    ...data.allData.map(row => {
                        const fullName = ((row.name || row.Name || '') + ' ' + (row.surname || row.Surname || '')).trim();
                        return [fullName || 'N/A'];
                    })
                ];
            } else if (tableId === 'table3') {
                fileName = 'Toplantidan_Kacanlar.xlsx';
                worksheetData = [
                    ['Katılımcı Adı Soyadı'],
                    ...data.allData.map(row => {
                        const fullName = ((row.name || row.Name || '') + ' ' + (row.surname || row.Surname || '')).trim();
                        return [fullName || 'N/A'];
                    })
                ];
            }

            // Excel dosyası oluştur
            const wb = XLSX.utils.book_new();
            const ws = XLSX.utils.aoa_to_sheet(worksheetData);
            XLSX.utils.book_append_sheet(wb, ws, 'Veriler');
            XLSX.writeFile(wb, fileName);
        }
    </script>
</body>
</html>'''

@app.route('/api/meetings')
def get_meetings():
    try:
        url = "http://rotary2627.cloud:8080/api/meeting/getAll"
        r = requests.get(url, headers=get_headers())
        return jsonify(r.json())
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/user-count', methods=['POST'])
def get_user_count():
    try:
        req_data = request.get_json()
        meeting_id = req_data.get('meeting_id')
        if not meeting_id:
            return jsonify({'count': 0})

        url = "http://rotary2627.cloud:8080/api/tracing/countCurrentUsers"
        payload = {"meetingId": meeting_id}
        
        r = requests.post(url, json=payload, headers=get_headers())
        r.raise_for_status()
        
        # API'den gelen cevabı kontrol et
        result = r.json()
        
        # Eğer direkt sayı dönüyorsa
        if isinstance(result, int):
            return jsonify({'count': result})
        # Eğer obje içinde count varsa
        elif isinstance(result, dict) and 'count' in result:
            return jsonify({'count': result['count']})
        else:
            return jsonify({'count': 0})
            
    except Exception as e:
        return jsonify({'error': str(e), 'count': 0}), 500

@app.route('/api/end-sessions', methods=['POST'])
def end_active_sessions():
    try:
        req_data = request.get_json()
        meeting_id = req_data.get('meeting_id')
        
        print(f"[END_SESSIONS] Received meeting_id: {meeting_id}")
        
        if not meeting_id:
            return jsonify({'error': 'Meeting ID gerekli'}), 400

        url = "http://rotary2627.cloud:8080/api/tracing/closeActiveSessions"
        payload = {"meetingId": meeting_id}
        
        r = requests.post(url, json=payload, headers=get_headers())
        r.raise_for_status()
        
        result = r.json() if r.text else {'success': True}
        
        return jsonify({'success': True, 'data': result})
            
    except requests.exceptions.HTTPError as e:
        error_msg = f"HTTP Error: {e.response.status_code} - {e.response.text}"
        print(f"[END_SESSIONS] HTTP Error: {error_msg}")
        return jsonify({'error': error_msg}), 500
    except Exception as e:
        error_msg = str(e)
        print(f"[END_SESSIONS] Exception: {error_msg}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': error_msg}), 500

@app.route('/api/data/table1', methods=['POST'])
def get_table1_data():
    try:
        req_data = request.get_json()
        meeting_id = req_data.get('meeting_id')
        if not meeting_id:
            return jsonify({'data': []})

        url = "http://rotary2627.cloud:8080/api/tracing/calculateTotalInsideDurationForAdmin"
        payload = {"meetingId": meeting_id}
        
        r = requests.post(url, json=payload, headers=get_headers())
        r.raise_for_status()
        
        return jsonify({'data': r.json()})
    except Exception as e:
        return jsonify({'error': str(e), 'data': []}), 500

@app.route('/api/data/table2', methods=['POST'])
def get_table2_data():
    try:
        req_data = request.get_json()
        meeting_id = req_data.get('meeting_id')
        
        if not meeting_id:
            print("[TABLE2] No meeting_id provided!")
            return jsonify({'data': []})

        url = "http://rotary2627.cloud:8080/api/participant/findAbsentParticipants"
        payload = {"meetingId": meeting_id}
        
        r = requests.post(url, json=payload, headers=get_headers())
        
        r.raise_for_status()
        
        result = r.json()
        print(f"[TABLE2] Result type: {type(result)}")
        print(f"[TABLE2] Result: {result}")
        
        # Eğer result bir dict ise ve içinde 'data' varsa onu al
        if isinstance(result, dict):
            if 'data' in result:
                result = result['data']
            elif 'participants' in result:
                result = result['participants']
            elif 'users' in result:
                result = result['users']
        
        return jsonify({'data': result if isinstance(result, list) else []})
    except requests.exceptions.HTTPError as e:
        error_msg = f"HTTP Error: {e.response.status_code} - {e.response.text}"
        print(f"[TABLE2] HTTP Error: {error_msg}")
        return jsonify({'error': error_msg, 'data': []}), 500
    except Exception as e:
        error_msg = str(e)
        print(f"[TABLE2] Exception: {error_msg}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': error_msg, 'data': []}), 500

@app.route('/api/data/table3', methods=['POST'])
def get_table3_data():
    try:
        req_data = request.get_json()
        meeting_id = req_data.get('meeting_id')
        minutes = req_data.get('minutes', 10)  # Default 10 dakika
        
        if not meeting_id:
            print("[TABLE3] No meeting_id provided!")
            return jsonify({'data': []})

        # Birden fazla endpoint denemesi yapacağız
        possible_urls = [
            "http://rotary2627.cloud:8080/api/participant/findUsersNotPresentInLastTenMinutes"
        ]
        
        payload = {"meetingId": meeting_id, "duration": minutes}
        result = None
        successful_url = None
        
        for url in possible_urls:
            try:
                print(f"[TABLE3] Trying URL: {url}")
                print(f"[TABLE3] Payload: {payload}")
                
                r = requests.post(url, json=payload, headers=get_headers())
                
                print(f"[TABLE3] Status Code: {r.status_code}")
                
                if r.status_code == 200:
                    print(f"[TABLE3] SUCCESS with URL: {url}")
                    print(f"[TABLE3] Response Text: {r.text}")
                    result = r.json()
                    successful_url = url
                    break
                else:
                    print(f"[TABLE3] Failed with status {r.status_code}, trying next URL...")
                    
            except Exception as e:
                print(f"[TABLE3] Failed with error: {str(e)}, trying next URL...")
                continue
        
        if result is None:
            print(f"[TABLE3] All URLs failed!")
            return jsonify({'error': 'Tüm endpoint denemeleri başarısız oldu. Lütfen doğru endpoint adını kontrol edin.', 'data': []}), 500
        
        print(f"[TABLE3] Result type: {type(result)}")
        print(f"[TABLE3] Result: {result}")
        
        # Eğer result bir dict ise ve içinde 'data' varsa onu al
        if isinstance(result, dict):
            if 'data' in result:
                result = result['data']
            elif 'participants' in result:
                result = result['participants']
            elif 'users' in result:
                result = result['users']
        
        return jsonify({'data': result if isinstance(result, list) else []})
    except requests.exceptions.HTTPError as e:
        error_msg = f"HTTP Error: {e.response.status_code} - {e.response.text}"
        print(f"[TABLE3] HTTP Error: {error_msg}")
        return jsonify({'error': error_msg, 'data': []}), 500
    except Exception as e:
        error_msg = str(e)
        print(f"[TABLE3] Exception: {error_msg}")
        import traceback
        traceback.print_exc()
        return jsonify({'error': error_msg, 'data': []}), 500

if __name__ == '__main__':
    app.run(debug=True, port=3000)
