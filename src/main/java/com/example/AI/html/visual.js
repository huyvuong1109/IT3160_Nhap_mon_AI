
// Khởi tạo bản đồ
let map = L.map('map', { zoomControl: false }).setView([21.0132381, 105.7821116], 14);
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
	maxZoom: 19
}).addTo(map);

// Chứa các đối tượng vẽ
let drawnItems = new L.FeatureGroup();
map.addLayer(drawnItems);

// Chứa các marker
let markersGroup = new L.FeatureGroup();
map.addLayer(markersGroup);

// Chứa route
let routeGroup = new L.FeatureGroup();
map.addLayer(routeGroup);

// Biến lưu rectangle
let currentRectangle = null;

// Biến lưu marker được chọn
let selectedMarkers = [];
let allMarkers = [];
let routeMarkers = [];

// Thanh công cụ vẽ
let drawControl = new L.Control.Draw({
	draw: {
		polygon: false,
		polyline: false,
		circle: false,
		marker: false,
		circlemarker: false,
		rectangle: {
			shapeOptions: {
				color: '#ff7800',
				weight: 2
			}
		}
	},
	edit: {
		featureGroup: drawnItems,
		remove: true
	}
});
map.addControl(drawControl);

// Xử lý vẽ
map.on(L.Draw.Event.CREATED, function (event) {
	let layer = event.layer;

	if (layer instanceof L.Rectangle) {
		if (currentRectangle) drawnItems.removeLayer(currentRectangle);
		currentRectangle = layer;
	}

	drawnItems.addLayer(layer);
});

// Xử lý xóa
map.on(L.Draw.Event.DELETED, function (event) {
	let layers = event.layers;
	layers.eachLayer(function (layer) {
		if (layer === currentRectangle) {
			currentRectangle = null;
		}
	});
});

// Hàm đọc file AI.intersections
async function loadIntersectionFile() {
	try {
		const response = await fetch('AI.intersections');
		if (!response.ok) {
			throw new Error(`HTTP error! status: ${response.status}`);
		}
		const content = await response.text();
		parseIntersectionData(content);
		console.log('Đã tải thành công file AI.intersections');
	} catch (error) {
		console.error('Lỗi khi đọc file AI.intersections:', error);
		alert('Không thể đọc file AI.intersections. Vui lòng đảm bảo file tồn tại trong cùng thư mục với trang web.');
	}
}

// Hàm đọc file route.txt
async function loadRouteFile() {

	const detailResponse = await fetch("http://localhost:8080/mapAI/findRoute", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		// body: JSON.stringify({
		// 	map: "", // Use the ID from the newly created order
		// }),
	});
	if (!detailResponse.ok) {
		const detailErr = await detailResponse.json();
		throw new Error(detailErr.message || 'Lỗi từ API');
	}
	try {
		const response = await fetch('route.txt');
		if (!response.ok) {
			throw new Error(`HTTP error! status: ${response.status}`);
		}
		const content = await response.text();
		parseRouteData(content);
		console.log('Đã tải thành công file route.txt');
	} catch (error) {
		console.error('Lỗi khi đọc file route.txt:', error);
		alert('Không thể đọc file route.txt. Vui lòng đảm bảo file tồn tại trong cùng thư mục với trang web.');
	}
}

// Hàm phân tích dữ liệu route
function parseRouteData(content) {
	clearRoute();

	const lines = content.trim().split('\n');
	let routeCoordinates = [];
	let routePointsCount = 0;

	lines.forEach((line, index) => {
		if (line.trim()) {
			const coords = line.trim().split(',');
			if (coords.length >= 2) {
				const lat = parseFloat(coords[0].trim());
				const lon = parseFloat(coords[1].trim());

				if (!isNaN(lat) && !isNaN(lon)) {
					routeCoordinates.push([lat, lon]);
					routePointsCount++;

					// Tạo marker cho điểm route
					const marker = L.circleMarker([lat, lon], {
						color: '#0066ff',
						fillColor: '#0066ff',
						fillOpacity: 0.8,
						weight: 3,
						radius: 8
					}).bindPopup(`
                        Điểm ${index + 1}<br>
                        Lat: ${lat}<br>
                        Lon: ${lon}
                    `);

					// Thêm số thứ tự cho marker
					const numberIcon = L.divIcon({
						className: 'route-number-icon',
						html: `<div style="background: #0066ff; color: white; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; border: 2px solid white;">${index + 1}</div>`,
						iconSize: [20, 20],
						iconAnchor: [10, 10]
					});
					const numberMarker = L.marker([lat, lon], { icon: numberIcon });
					routeGroup.addLayer(marker);
					routeGroup.addLayer(numberMarker);
					routeMarkers.push(marker);
				}
			}
		}
	});

	// Vẽ đường nối giữa các điểm
	if (routeCoordinates.length > 1) {
		const routeLine = L.polyline(routeCoordinates, {
			color: '#0066ff',
			weight: 4,
			opacity: 0.8,
			dashArray: '0'
		});

		routeGroup.addLayer(routeLine);
	}

	// Cập nhật thống kê
	document.getElementById('routePoints').textContent = routePointsCount;
	// Điều chỉnh view để hiển thị route
	if (routeCoordinates.length > 0) {
		const group = new L.featureGroup(routeGroup.getLayers());
		map.fitBounds(group.getBounds().pad(0.1));
	}
	console.log(`Đã tải route với ${routePointsCount} điểm`);
}


// Hàm xóa route
function clearRoute() {
	routeGroup.clearLayers();
	routeMarkers = [];
	document.getElementById('routePoints').textContent = '0';
}

// Hàm xử lý click marker
function onMarkerClick(marker, coordinates) {
	// Nếu marker đã được chọn, bỏ chọn
	if (selectedMarkers.includes(marker)) {
		unselectMarker(marker);
		return;
	}

	// Nếu đã chọn đủ 2 marker, bỏ chọn marker đầu tiên
	if (selectedMarkers.length >= 2) {
		unselectMarker(selectedMarkers[0]);
	}

	// Chọn marker mới
	selectMarker(marker, coordinates);
	updateSelectedMarkersDisplay();
}

// Hàm chọn marker
function selectMarker(marker, coordinates) {
	selectedMarkers.push(marker);

	// Thay đổi style để hiển thị marker được chọn
	marker.setStyle({
		color: '#00ff00',
		fillColor: '#00ff00',
		weight: 4,
		radius: 8
	});

	// Lưu tọa độ vào marker
	marker._coordinates = coordinates;
}

// Hàm bỏ chọn marker
function unselectMarker(marker) {
	const index = selectedMarkers.indexOf(marker);
	if (index > -1) {
		selectedMarkers.splice(index, 1);
	}

	// Khôi phục style ban đầu
	marker.setStyle({
		color: 'red',
		fillColor: 'red',
		fillOpacity: 0.8,
		weight: 3,
		radius: 6
	});
}

// Hàm cập nhật hiển thị marker được chọn
function updateSelectedMarkersDisplay() {
	const display = document.getElementById('selectedMarkers');
	if (selectedMarkers.length === 0) {
		display.innerHTML = 'Chưa chọn marker nào';
	} else if (selectedMarkers.length === 1) {
		const coord = selectedMarkers[0]._coordinates;
		display.innerHTML = `Đã chọn 1 marker: Lat: ${coord.lat}, Lon: ${coord.lon}`;
	} else {
		const coord1 = selectedMarkers[0]._coordinates;
		const coord2 = selectedMarkers[1]._coordinates;
		display.innerHTML = `Đã chọn 2 marker:<br>
			1. Lat: ${coord1.lat}, Lon: ${coord1.lon}<br>
			2. Lat: ${coord2.lat}, Lon: ${coord2.lon}`;
	}
}

// Hàm phân tích dữ liệu intersection
function parseIntersectionData(content) {
	clearMarkers();

	const lines = content.trim().split('\n');
	let totalPoints = 0;
	let coordinatePairs = 0;
	let allCoordinates = [];

	const colors = ['red'];
	let colorIndex = 0;

	lines.forEach((line, index) => {
		if (line.trim()) {
			const coords = extractCoordinates(line);
			if (coords && coords.length >= 2) {
				coordinatePairs++;

				// Lấy màu cho cặp điểm này
				const currentColor = colors[0];

				const marker1 = L.circleMarker([coords[0].lat, coords[0].lon], {
					color: currentColor,
					fillColor: currentColor,
					fillOpacity: 0.8,
					weight: 3,
					radius: 6
				}).bindPopup(`
                    Lat: ${coords[0].lat}<br>
                    Lon: ${coords[0].lon}
                `);

				const marker2 = L.circleMarker([coords[1].lat, coords[1].lon], {
					color: currentColor,
					fillColor: currentColor,
					fillOpacity: 0.8,
					weight: 3,
					radius: 6
				}).bindPopup(`
                    Lat: ${coords[1].lat}<br>
                    Lon: ${coords[1].lon}
                `);

				// Thêm sự kiện click cho marker
				marker1.on('click', function() {
					onMarkerClick(marker1, coords[0]);
				});

				marker2.on('click', function() {
					onMarkerClick(marker2, coords[1]);
				});

				// Vẽ đường nối giữa 2 điểm
				const line = L.polyline([
					[coords[0].lat, coords[0].lon],
					[coords[1].lat, coords[1].lon]
				], {
					color: currentColor,
					weight: 2,
					opacity: 0.6,
					dashArray: '5, 5'
				});

				markersGroup.addLayer(marker1);
				markersGroup.addLayer(marker2);
				markersGroup.addLayer(line);

				// Lưu marker vào danh sách
				allMarkers.push(marker1, marker2);

				allCoordinates.push(coords[0], coords[1]);
				totalPoints += 2;
			}
		}
	});

	// Cập nhật thống kê
	document.getElementById('totalPoints').textContent = totalPoints;
	document.getElementById('coordinatePairs').textContent = coordinatePairs;

	// Điều chỉnh view để hiển thị tất cả điểm (chỉ khi không có route)
	if (allCoordinates.length > 0 && routeMarkers.length === 0) {
		const group = new L.featureGroup(markersGroup.getLayers());
		map.fitBounds(group.getBounds().pad(0.1));
	}

	console.log(`Đã tải ${coordinatePairs} cặp tọa độ, tổng ${totalPoints} điểm`);
}

// Hàm trích xuất tọa độ từ dòng text
function extractCoordinates(line) {
	const regex = /Lat:\s*([\d.-]+),\s*Lon:\s*([\d.-]+)/g;
	const coordinates = [];
	let match;

	while ((match = regex.exec(line)) !== null) {
		coordinates.push({
			lat: parseFloat(match[1]),
			lon: parseFloat(match[2])
		});
	}

	return coordinates;
}

// Hàm xóa tất cả marker intersection
function clearMarkers() {
	markersGroup.clearLayers();
	selectedMarkers = [];
	allMarkers = [];
	document.getElementById('totalPoints').textContent = '0';
	document.getElementById('coordinatePairs').textContent = '0';
	updateSelectedMarkersDisplay();
}

// Hàm xóa tất cả (cả intersection và route)
function clearAll() {
	clearMarkers();
	clearRoute();
}

// Hàm lưu tọa độ của 2 marker được chọn
async function saveSelectedCoordinates() {
	if (selectedMarkers.length !== 2) {
		alert('Vui lòng chọn đúng 2 marker trước khi lưu.');
		return;
	}

	const coord1 = selectedMarkers[0]._coordinates;
	const coord2 = selectedMarkers[1]._coordinates;

	const coordinatesText = `${coord1.lat} ${coord1.lon}
 ${coord2.lat} ${coord2.lon}`;
	const detailResponse = await fetch("http://localhost:8080/mapAI/saveLocation", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		body: JSON.stringify({
			map: coordinatesText, // Use the ID from the newly created order
		}),
	});
	if (!detailResponse.ok) {
		const detailErr = await detailResponse.json();
		throw new Error(detailErr.message || 'Lỗi từ API');
	}
	alert("Đã lưu tọa độ của 2 marker được chọn");
}

// Hàm xóa lựa chọn marker
function clearSelection() {
	selectedMarkers.forEach(marker => {
		unselectMarker(marker);
	});
	selectedMarkers = [];
	updateSelectedMarkersDisplay();
}

async function saveBoundsToFile() {
	if (drawnItems.getLayers().length === 0) {
		alert("Bạn cần vẽ một vùng trước.");
		return;
	}

	let bounds = drawnItems.getBounds();
	let boundsXml = `<bounds minlat="${bounds.getSouth().toFixed(7)}" minlon="${bounds.getWest().toFixed(7)}" maxlat="${bounds.getNorth().toFixed(7)}" maxlon="${bounds.getEast().toFixed(7)}"/>`;

	let osmContent = `<?xml version="1.0" encoding="UTF-8"?>
<osm version="0.6" generator="LeafletDrawTool">
  ${boundsXml}
</osm>`;

	const detailResponse = await fetch("http://localhost:8080/mapAI/createMap", {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		body: JSON.stringify({
			map: osmContent, // Use the ID from the newly created order
		}),
	});
	if (!detailResponse.ok) {
		const detailErr = await detailResponse.json();
		throw new Error(detailErr.message || 'Lỗi từ API');
	}
	alert("Load map done");
}
window.onload = function() {
	console.log("Đang tải dữ liệu...");
};
